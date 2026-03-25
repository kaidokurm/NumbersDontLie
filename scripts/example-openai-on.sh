#!/usr/bin/env bash
# Set OpenAI environment variables
set -euo pipefail

export OPENAI_API_KEY="sk-proj-REPLACE_ME"
export OPENAI_MODEL="gpt-4o-mini"

echo "OpenAI environment variables set."
echo "OPENAI_API_KEY is set to ${OPENAI_API_KEY:0:14}... (hidden for security)"
echo "OPENAI_MODEL is set to $OPENAI_MODEL"