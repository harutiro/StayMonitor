#!/bin/sh
# Claude Code PostToolUse hook
# ios/ 配下の .swift を swift-format(Apple) で整形し、SwiftLint(--strict) で検査する。
# - swift-format: Xcode 同梱の `xcrun swift-format` を使用（追加インストール不要）
# - swiftlint:    違反があれば {"decision":"block"} で差し戻す
# 依存: jq

FILE_PATH=$(jq -r '.tool_input.file_path // ""')
[ -n "$FILE_PATH" ] || exit 0

# .swift のみ対象
case "$FILE_PATH" in
  *.swift) ;;
  *) exit 0 ;;
esac

SRCROOT=$(git rev-parse --show-toplevel 2>/dev/null) || exit 0

# ios/ 配下限定（iOS のみに適用）
case "$FILE_PATH" in
  "$SRCROOT"/ios/*) ;;
  *) exit 0 ;;
esac

# ファイルが存在しなければ skip
[ -f "$FILE_PATH" ] || exit 0

# 1. swift-format（Apple, Xcode 同梱）で整形。利用不可なら skip
if xcrun --find swift-format >/dev/null 2>&1; then
  if [ -f "$SRCROOT/ios/.swift-format" ]; then
    xcrun swift-format format --in-place --configuration "$SRCROOT/ios/.swift-format" "$FILE_PATH" >/dev/null 2>&1 || true
  else
    xcrun swift-format format --in-place "$FILE_PATH" >/dev/null 2>&1 || true
  fi
fi

# 2. SwiftLint（--strict）。違反は block で差し戻す
if command -v swiftlint >/dev/null 2>&1 && [ -f "$SRCROOT/ios/.swiftlint.yml" ]; then
  LINT_OUTPUT=$(swiftlint lint --strict --quiet --config "$SRCROOT/ios/.swiftlint.yml" "$FILE_PATH" 2>&1)
  if [ $? -ne 0 ]; then
    [ -n "$LINT_OUTPUT" ] || LINT_OUTPUT="SwiftLint failed (no output)."
    jq -n --arg reason "$LINT_OUTPUT" '{"decision":"block","reason":$reason}'
    exit 0
  fi
fi

exit 0
