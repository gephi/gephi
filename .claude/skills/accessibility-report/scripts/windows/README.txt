Gephi accessibility report: Windows kit for Gephi {version}

Unzip this folder somewhere without spaces in the path, e.g. C:\gephi-a11y

1. Automated run (about 5 minutes)
   - Install Gephi {version} from https://github.com/gephi/gephi/releases/tag/v{version}
   - Close NVDA if it is running.
   - Open PowerShell in this folder and run:
         powershell -ExecutionPolicy Bypass -File .\measure-windows.ps1
     (add -Gephi "<install folder>" if Gephi isn't installed in Program Files)
   - Don't touch the keyboard or mouse until the script says it's done.
   - Send back windows-measurements.zip

2. Screen reader check (about 20 minutes)
   - Follow checklist.md with NVDA, writing your results into it as you go (Notepad is fine).
   - Send back the filled checklist.md and the NVDA Speech Viewer text.
