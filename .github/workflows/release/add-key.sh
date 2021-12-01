#!/bin/sh

# Create a custom keychain
security create-keychain -p travis gephi-build.keychain

# Make the custom keychain default, so xcodebuild will use it for signing
security default-keychain -s gephi-build.keychain

# Unlock the keychain
security unlock-keychain -p travis gephi-build.keychain

# Set keychain timeout to 1 hour for long builds
security set-keychain-settings -t 3600 -l ~/Library/Keychains/gephi-build.keychain

# Add certificates to keychain and allow codesign to access them
security import ./.github/workflows/release/certs/apple.cer -k ~/Library/Keychains/gephi-build.keychain -T /usr/bin/codesign
security import ./.github/workflows/release/certs/dev_id.cer -k ~/Library/Keychains/gephi-build.keychain -T /usr/bin/codesign
security import ./.github/workflows/release/certs/dev_id.p12 -k ~/Library/Keychains/gephi-build.keychain -P $KEY_PASSWORD -T /usr/bin/codesign

security set-key-partition-list -S apple-tool:,apple: -s -k travis gephi-build.keychain