#!/usr/bin/env bash
# Unset OpenAI environment variables
set -euo pipefail

unset OPENAI_API_KEY
unset OPENAI_MODEL

echo "OpenAI environment variables unset."