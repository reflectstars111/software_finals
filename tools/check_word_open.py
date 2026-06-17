from __future__ import annotations

import sys
from pathlib import Path

import win32com.client


def main() -> int:
    path = Path(sys.argv[1]).resolve()
    word = win32com.client.DispatchEx("Word.Application")
    word.Visible = False
    word.DisplayAlerts = 0
    word.AutomationSecurity = 3
    document = None
    try:
        document = word.Documents.Open(
            str(path),
            ConfirmConversions=False,
            ReadOnly=True,
            AddToRecentFiles=False,
            Visible=False,
            OpenAndRepair=False,
        )
        print(
            f"OK pages={document.ComputeStatistics(2)} "
            f"paragraphs={document.Paragraphs.Count} tables={document.Tables.Count}"
        )
        return 0
    except Exception as error:
        print(f"FAIL {error}")
        return 1
    finally:
        if document is not None:
            document.Close(False)
        word.Quit()


if __name__ == "__main__":
    raise SystemExit(main())
