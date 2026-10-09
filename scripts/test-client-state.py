#!/usr/bin/env python3
"""Exercise synced HUD state from the exact representative release JARs.

These are JVM state checks. World combat, graphics and input still need the
separate runtime/manual regression checklist.
"""
from pathlib import Path
import os
import runpy
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
HARNESS = r'''
import net.vg.combo.client.ComboClientState;
import net.vg.combo.client.ComboStaminaClient;

public class ComboStateSmoke {
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(ComboClientState.stage() == 0, "HUD starts idle");
        ComboClientState.update(1, 12, false);
        ComboClientState.tick();
        check(ComboClientState.stage() == 1 && !ComboClientState.finisher(), "confirmed hit displays first pip");
        ComboClientState.update(2, 12, false);
        check(ComboClientState.remainingTicks() == 12, "new hit refreshes timeout");
        for (int i = 0; i < 13; i++) ComboClientState.tick();
        check(ComboClientState.stage() == 0 && !ComboClientState.finisher(), "idle timeout clears pips");
        ComboClientState.update(3, 0, true);
        ComboClientState.tick();
        check(ComboClientState.stage() == 3 && ComboClientState.finisher(), "finisher flashes despite zero server timeout");
        for (int i = 0; i < 16; i++) ComboClientState.tick();
        check(ComboClientState.stage() == 0 && !ComboClientState.finisher(), "finisher flash expires");
        ComboClientState.update(3, 0, true);
        ComboClientState.update(1, 12, false);
        check(ComboClientState.stage() == 1 && !ComboClientState.finisher(), "next chain replaces finisher flash");
        ComboStaminaClient.update(35.5F, 100.0F);
        check(ComboStaminaClient.current() == 35.5F && ComboStaminaClient.max() == 100.0F, "stamina sync retains configured scale");
        ComboStaminaClient.update(100.0F, 100.0F);
        check(ComboStaminaClient.current() == ComboStaminaClient.max(), "full stamina sync permits HUD auto-hide");
    }
}
'''


def main():
    wrapper = runpy.run_path(str(ROOT / 'build-smart.py'))
    verifier = runpy.run_path(str(ROOT / 'scripts/verify-matrix-artifacts.py'))
    project = wrapper['Project'](ROOT)
    java_home, _, fatal = wrapper['select_gradle_java'](project, [])
    if fatal:
        raise SystemExit('A native Java 25 JDK is required; use the established Windows environment.')
    java = str(wrapper['java_binary'](java_home)) if java_home else 'java'
    javac = str(Path(java).with_name('javac.exe' if os.name == 'nt' else 'javac')) if java_home else 'javac'
    targets = [target for target in project.targets if target.props.get('release_smoke') == 'true']
    if not targets:
        raise SystemExit('No representative release targets configured')
    with tempfile.TemporaryDirectory(prefix='combo-state-') as temporary:
        directory = Path(temporary)
        source = directory / 'ComboStateSmoke.java'
        source.write_text(HARNESS)
        for target in targets:
            artifact = verifier['release_jar'](ROOT / 'gradle/matrix' / (target.name + '.properties'))
            compiled = directory / target.name
            compiled.mkdir()
            for command in ([javac, '--release', str(target.java), '-cp', str(artifact), '-d', str(compiled), str(source)],
                            [java, '-cp', os.pathsep.join((str(compiled), str(artifact))), 'ComboStateSmoke']):
                result = subprocess.run(command, capture_output=True, text=True, timeout=60)
                if result.returncode:
                    raise SystemExit(target.name + ': ' + result.stderr[-2000:])
            print(target.name + ': combo timeout, finisher flash, next-chain and stamina sync state PASS')
    print(f'CLIENT STATE PASS ({len(targets)}/{len(targets)} representative release JARs)')


if __name__ == '__main__':
    main()
