#!/usr/bin/env python3
"""Verify Combo's installable matrix JARs, not gameplay behavior."""
import json
from pathlib import Path
import re
import struct
from zipfile import BadZipFile, ZipFile

try:
    import tomllib
except ImportError:  # The established wrapper also supports Python 3.10.
    tomllib = None

ROOT = Path(__file__).resolve().parents[1]
MOD_ID = 'combo'
PACKAGE = 'net/vg/combo/'


def properties(path):
    return {key.strip(): value.strip() for line in path.read_text().splitlines()
            if '=' in line and not line.lstrip().startswith('#')
            for key, value in [line.split('=', 1)]}


def quoted(text, key):
    match = re.search(rf'^\s*{re.escape(key)}\s*=\s*"([^"\n]*)"', text, re.MULTILINE)
    assert match, f'Missing metadata field: {key}'
    return match.group(1)


def release_jar(matrix):
    """Select exactly one expected release; reject classifiers and stale versions."""
    jars = [p for p in (ROOT / 'build/libs' / matrix.stem).glob('*.jar')
            if not p.name.endswith(('-dev.jar', '-sources.jar', '-javadoc.jar',
                                    '-dev-shadow.jar', '-raw.jar'))]
    assert len(jars) == 1, f'Expected one release JAR: {jars}'
    root_pins = properties(ROOT / 'gradle.properties')
    minecraft, loader = matrix.stem.rsplit('-', 1)
    expected = f"{root_pins['archives_name']}-{loader}-{minecraft}-{root_pins['mod_version']}.jar"
    assert jars[0].name == expected, f'Wrong/stale release artifact: {jars[0]} (expected {expected})'
    return jars[0]


def inspect(matrix):
    pins = properties(matrix)
    assert matrix.stem == f"{pins['minecraft_version']}-{pins['loader']}", 'Target/property mismatch'
    artifact = release_jar(matrix)
    legacy = pins['loom_generation'] == 'legacy'
    root_pins = properties(ROOT / 'gradle.properties')
    with ZipFile(artifact) as jar:
        entries = jar.namelist()
        files = set(entries)
        assert len(entries) == len(files), 'Duplicate archive entries'
        read_json = lambda name: json.loads(jar.read(name))
        required_classes = (
            'Combo', 'config/ComboConfig', 'client/ComboConfigScreen', 'client/ComboHud',
            'client/ComboDamagePopups', 'client/ComboStaggerEffects', 'client/ComboVignetteEffects',
            'client/ComboDodgeClient', 'combat/ComboCombat', 'combat/ComboState',
            'combat/ComboParry', 'combat/ComboStamina', 'combat/ComboDodge', 'combat/WeaponCategory',
            'stats/ComboStats', 'network/ComboSyncPayload', 'network/ComboDamagePopupPayload',
            'network/ComboStaggerPayload', 'network/ComboBackstabPayload',
            'network/ComboStaminaPayload', 'network/ComboDodgeRequestPayload',
        )
        for name in required_classes:
            assert PACKAGE + name + '.class' in files, f'Missing mod class: {name}'
        assert (PACKAGE + 'client/ComboWorldText.class' in files) == legacy, 'Wrong world-text compatibility implementation'
        classes = [name for name in files if name.endswith('.class')]
        assert classes and all(name.startswith(PACKAGE) for name in classes), 'Bundled dependency classes'
        assert all(name.split('/')[1] == MOD_ID for name in files
                   if name.startswith(('assets/', 'data/')) and len(name.split('/')) > 2), 'Bundled external resources/datapack'
        for name in classes:
            bytecode = jar.read(name)
            assert bytecode[:4] == b'\xca\xfe\xba\xbe', f'Invalid class: {name}'
            assert struct.unpack('>H', bytecode[6:8])[0] == int(pins['java_version']) + 44, f'Wrong class level: {name}'
        for name in files:
            if name.endswith('.json'):
                read_json(name)
        mixins = read_json('combo.mixins.json')
        assert mixins['required'] is True
        assert mixins['compatibilityLevel'] == 'JAVA_' + pins['java_version']
        assert mixins['mixins'] == ['PlayerAttackMixin', 'LivingEntityBlockMixin', 'BlocksAttacksMixin', 'StealthVisibilityMixin'], 'Missing combat injections'
        assert not mixins.get('client') and not mixins.get('server')
        assert mixins['package'] == 'net.vg.combo.mixin'
        for side in ('mixins', 'client', 'server'):
            for mixin in mixins.get(side, []):
                name = (mixins['package'] + '.' + mixin).replace('.', '/') + '.class'
                assert name in files, f'Missing mixin class: {name}'
        if pins['loader'] == 'fabric':
            assert 'META-INF/neoforge.mods.toml' not in files, 'Wrong loader metadata'
            meta = read_json('fabric.mod.json')
            assert meta['id'] == MOD_ID and meta['version'] == root_pins['mod_version']
            assert meta['depends']['minecraft'] == pins['minecraft_version']
            assert set(meta['depends']) == {'java', 'minecraft', 'fabricloader', 'architectury', 'fabric-api'}
            for dep, pin in (('java', 'java_version'), ('fabricloader', 'fabric_loader_version'),
                             ('architectury', 'architectury_api_version')):
                assert meta['depends'][dep] == '>=' + pins[pin], f'Wrong dependency: {dep}'
            assert meta['suggests'] == {'modmenu': '*'}
            assert meta['mixins'] == ['combo.mixins.json']
            assert meta['environment'] == '*'
            assert meta['entrypoints'] == {
                'main': ['net.vg.combo.fabric.ComboFabric'],
                'client': ['net.vg.combo.fabric.client.ComboFabricClient'],
                'modmenu': ['net.vg.combo.fabric.client.ComboModMenu'],
            }
            for names in meta['entrypoints'].values():
                for name in names:
                    assert name.replace('.', '/') + '.class' in files, f'Missing entrypoint: {name}'
            assert meta['icon'] in files
            assert 'accessWidener' not in meta
            if legacy:
                assert b'net/minecraft/class_' in jar.read(PACKAGE + 'client/ComboHud.class'), 'Unremapped Fabric release'

        else:
            assert 'fabric.mod.json' not in files, 'Wrong loader metadata'
            meta = jar.read('META-INF/neoforge.mods.toml').decode()
            assert quoted(meta, 'modId') == MOD_ID
            assert quoted(meta, 'version') == root_pins['mod_version']
            assert PACKAGE + 'neoforge/ComboNeoForge.class' in files
            assert PACKAGE + 'neoforge/client/ComboNeoForgeClient.class' in files
            bootstrap = jar.read(PACKAGE + 'neoforge/ComboNeoForge.class')
            assert b'net/minecraft/client/' not in bootstrap and b'net/neoforged/neoforge/client/' not in bootstrap, 'Client types in dedicated-server bootstrap'
            assert b'CLIENT' in jar.read(PACKAGE + 'neoforge/client/ComboNeoForgeClient.class'), 'Unrestricted client bootstrap'
            blocks = re.findall(r'\[\[dependencies\.combo\]\](.*?)(?=\n\[|\Z)', meta, re.DOTALL)
            deps = {quoted(block, 'modId'): block for block in blocks}
            assert set(deps) == {'minecraft', 'neoforge', 'architectury'}
            assert quoted(deps['minecraft'], 'versionRange') == '[' + pins['minecraft_version'] + ']'
            for dep, pin in (('neoforge', 'neoforge_version'), ('architectury', 'architectury_api_version')):
                assert quoted(deps[dep], 'versionRange') == '[' + pins[pin] + ',)'
            assert all(quoted(deps[dep], 'type') == 'required' for dep in ('minecraft', 'neoforge', 'architectury'))
            assert all(quoted(block, 'side') == 'BOTH' for block in deps.values())
            if tomllib is not None:
                tomllib.loads(meta)
            assert quoted(meta, 'javaVersion') == '[' + pins['java_version'] + ',)'
            icon_field = 'iconFile' if pins['minecraft_version'] == '26.2' else 'logoFile'
            assert quoted(meta, icon_field) in files
            assert quoted(meta, 'config') == 'combo.mixins.json'
        assert 'assets/combo/lang/en_us.json' in files
        assert any(name.startswith('LICENSE') for name in files)
        assert not any(name.endswith(('.jar', '.java')) for name in files), 'Embedded JAR or source archive'
        for name in ('fabric.mod.json', 'META-INF/neoforge.mods.toml', 'combo.mixins.json'):
            if name in files:
                assert b'${' not in jar.read(name), f'Unexpanded metadata: {name}'
    print(f'{matrix.stem}: release metadata, classes, mixins and resources OK')
    return artifact


if __name__ == '__main__':
    matrices = sorted((ROOT / 'gradle/matrix').glob('*.properties'))
    assert matrices, 'No matrix properties found'
    for matrix in matrices:
        try:
            inspect(matrix)
        except (AssertionError, KeyError, ValueError, OSError, BadZipFile) as error:
            raise SystemExit(f'{matrix.stem}: {error}') from error
    print(f'ARTIFACT VERIFICATION PASS ({len(matrices)}/{len(matrices)})')
