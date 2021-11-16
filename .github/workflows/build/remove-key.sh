#!/bin/sh
security delete-keychain gephi-build.keychain
rm -rf .github/workflows/build/certs
