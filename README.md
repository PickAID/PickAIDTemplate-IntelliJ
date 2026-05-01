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

## Local Use

Build the plugin ZIP:

```bash
./gradlew buildPlugin
```

Install `build/distributions/PickAIDTemplate-IntelliJ-0.1.0.zip` in IntelliJ IDEA:

1. Open Settings / Preferences.
2. Open Plugins.
3. Click the gear button.
4. Choose Install Plugin from Disk.
5. Select the ZIP and restart the IDE if prompted.

The plugin activates for `project.toml` and `project.local.toml` files that look like PickAID template configuration files.

## Publish

For the first JetBrains Marketplace release, create the plugin page manually and upload the ZIP from `build/distributions/`. After that, Gradle publishing can upload new versions.

Set the Marketplace token and publish:

```bash
export PUBLISH_TOKEN="<marketplace-token>"
./gradlew publishPlugin
```

To publish to a non-default channel:

```bash
export PUBLISH_TOKEN="<marketplace-token>"
export PUBLISH_CHANNEL="eap"
./gradlew publishPlugin
```

Optional signing uses environment variables only:

```bash
export CERTIFICATE_CHAIN="$(base64 -i chain.crt)"
export PRIVATE_KEY="$(base64 -i private.pem)"
export PRIVATE_KEY_PASSWORD="<private-key-password>"
export PUBLISH_TOKEN="<marketplace-token>"
./gradlew publishPlugin
```

Never commit Marketplace tokens, private keys, or certificates.
