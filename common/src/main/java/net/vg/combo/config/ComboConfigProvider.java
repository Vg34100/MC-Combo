package net.vg.combo.config;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ComboConfigProvider implements SimpleConfig.DefaultConfig {

    private final List<Pair<String, List<String>>> comments = new ArrayList<>();
    private final List<Pair<String, ?>> configsList = new ArrayList<>();

    public void addKeyValuePair(Pair<String, ?> keyValuePair, List<String> comment) {
        configsList.add(keyValuePair);
        comments.add(new Pair<>(keyValuePair.getFirst(), comment));
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (Pair<String, ?> pair : configsList) {
            keys.add(pair.getFirst());
        }
        return keys;
    }

    @Override
    public String get(String namespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Combo Settings\n\n");
        for (int i = 0; i < configsList.size(); i++) {
            Pair<String, ?> config = configsList.get(i);
            List<String> comment = comments.get(i).getSecond();
            for (String line : comment) {
                sb.append("# ").append(line).append("\n");
            }
            sb.append(config.getFirst()).append("=").append(config.getSecond()).append("\n\n");
        }
        return sb.toString();
    }
}
