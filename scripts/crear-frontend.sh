#!/bin/sh
set -e
cd "$(dirname "$0")/.."
export NG_CLI_ANALYTICS=false
export CI=true
npx --yes @angular/cli@latest new frontend \
  --defaults \
  --style=css \
  --ssr=false \
  --skip-git \
  --package-manager=npm
