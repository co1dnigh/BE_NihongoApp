# Codebase Exploration — Use `codebase-memory-mcp`

- Before reading/grepping multiple files to find a function, trace callers/callees, or assess the impact of a change, use the `codebase-memory-mcp` MCP tools (`search_graph`, `trace_path`, `get_code_snippet`, `detect_changes`, `get_architecture`) instead of manually reading files. It returns precise structural results at a fraction of the token cost.
- Use `trace_path` (direction="both") before modifying a shared service/method to see what depends on it.
- Use `detect_changes()` to map the current git diff to affected symbols before declaring a change safe.
- Fall back to Read/Grep only when the graph doesn't cover the answer (e.g. non-indexed file types, or `check_index_coverage` reports a gap).
