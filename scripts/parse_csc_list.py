#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
解析 research/samsung-csc-codes/README.md 的 CSC 代码表，
生成 app/src/main/assets/csc_list.json（供 Compose UI 搜索/选择）。

用法: python scripts/parse_csc_list.py
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "research", "samsung-csc-codes", "README.md")
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "csc_list.json")

# 行格式: | CODE | Region/Country/Provider |
ROW = re.compile(r"^\|\s*([A-Z0-9]{3})\s*\|\s*(.+?)\s*\|")


def main() -> None:
    entries = []
    with open(SRC, encoding="utf-8") as f:
        for line in f:
            m = ROW.match(line.strip())
            if not m:
                continue
            code, region = m.group(1), m.group(2)
            entries.append({"code": code, "region": region})

    # 去重并排序
    seen = {}
    for e in entries:
        seen.setdefault(e["code"], e["region"])
    result = [
        {"code": k, "region": v}
        for k, v in sorted(seen.items(), key=lambda x: x[0])
    ]

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, separators=(",", ":"))

    print(f"parsed {len(entries)} rows, unique {len(result)} codes -> {OUT}")


if __name__ == "__main__":
    main()
