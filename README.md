# PickAID Template Assistant

IntelliJ IDEA plugin for PickAID Minecraft templates.

The first version focuses on `project.toml` and `project.local.toml` editing:

- table, key, and value completion for PickAID template configuration
- inspections for unknown tables/keys, invalid `mod_id`, missing required `[mod]` fields, native library layout, and Forge 1.20.1 KubeJS coordinates
- quick fixes for the KubeJS coordinate and missing native platform directories

## Development

```bash
./gradlew build
./gradlew runIde
```

The plugin targets IntelliJ IDEA `2024.3.6+` and depends on the bundled TOML plugin.
