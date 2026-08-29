import os
import sys
import time
import shutil
import zipfile
import subprocess
import urllib.request
from datetime import datetime
from pathlib import Path

# Paths
PROJECT_DIR = Path(__file__).resolve().parent
BUILDS_DIR = PROJECT_DIR / "builds"
ADB_PATH = PROJECT_DIR / "tools" / "platform-tools" / "adb.exe"

REPO_OWNER = "Mohd-Amir-ai"
REPO_NAME = "MiniMalPhone"
ZIP_URL = f"https://github.com/{REPO_OWNER}/{REPO_NAME}/releases/download/latest/MiniMalPhone-debug.zip"
APK_URL = f"https://github.com/{REPO_OWNER}/{REPO_NAME}/releases/download/latest/MiniMalPhone-debug.apk"
ACTIONS_URL = f"https://github.com/{REPO_OWNER}/{REPO_NAME}/actions"

# Colors for terminal output
CYAN = "\033[96m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
RED = "\033[91m"
BOLD = "\033[1m"
RESET = "\033[0m"


def clear_screen():
    os.system("cls" if os.name == "nt" else "clear")


def print_banner():
    clear_screen()
    print(f"{CYAN}{BOLD}======================================================{RESET}")
    print(f"{CYAN}{BOLD}      MiniMalPhone — Device & Build Manager          {RESET}")
    print(f"{CYAN}{BOLD}======================================================{RESET}\n")


def check_adb_devices():
    """Check and display connected ADB devices."""
    if not ADB_PATH.exists():
        print(f"{RED}[ERROR] ADB not found at {ADB_PATH}{RESET}")
        return []

    try:
        result = subprocess.run([str(ADB_PATH), "devices", "-l"], capture_output=True, text=True)
        lines = result.stdout.strip().split("\n")
        devices = []
        for line in lines[1:]:
            line = line.strip()
            if line and not line.startswith("*"):
                devices.append(line)
        return devices
    except Exception as e:
        print(f"{RED}[ERROR] Failed to run ADB: {e}{RESET}")
        return []


def prune_build_folders(max_keep=3):
    """Keep only the latest max_keep build folders in the builds directory."""
    if not BUILDS_DIR.exists():
        return

    subdirs = [p for p in BUILDS_DIR.iterdir() if p.is_dir() and p.name.startswith("build_")]
    subdirs.sort(key=lambda x: x.stat().st_mtime, reverse=True)

    if len(subdirs) > max_keep:
        to_delete = subdirs[max_keep:]
        for folder in to_delete:
            print(f"{YELLOW}  [Cleanup] Removing old build folder: {folder.name}{RESET}")
            shutil.rmtree(folder, ignore_errors=True)
    print(f"{GREEN}  [Storage] Retaining last {min(len(subdirs), max_keep)} build folders in builds/{RESET}")


def download_latest_build():
    """Download the latest APK/Zip from GitHub release and extract it."""
    BUILDS_DIR.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    target_dir = BUILDS_DIR / f"build_{timestamp}"
    target_dir.mkdir(parents=True, exist_ok=True)

    zip_path = target_dir / "MiniMalPhone-debug.zip"
    apk_path = target_dir / "MiniMalPhone-debug.apk"

    print(f"{CYAN}Downloading latest package from GitHub Release...{RESET}")
    try:
        # Download ZIP
        urllib.request.urlretrieve(ZIP_URL, zip_path)
        if zip_path.exists() and zip_path.stat().st_size > 5000:
            print(f"{GREEN}Downloaded ZIP successfully ({zip_path.stat().st_size // 1024} KB).{RESET}")
            print(f"{CYAN}Extracting APK package...{RESET}")
            with zipfile.ZipFile(zip_path, "r") as z:
                z.extractall(target_dir)
            print(f"{GREEN}Extracted to: {target_dir}{RESET}")
        else:
            print(f"{YELLOW}Zip not found or empty, attempting direct APK download...{RESET}")
            urllib.request.urlretrieve(APK_URL, apk_path)
            print(f"{GREEN}Downloaded APK successfully.{RESET}")

    except Exception as e:
        print(f"{YELLOW}Release download note: {e}{RESET}")
        print(f"If the GitHub Actions workflow is still building, please wait ~1 minute and retry.")
        return None

    prune_build_folders(max_keep=3)

    # Locate extracted apk
    apk_candidates = list(target_dir.glob("*.apk"))
    if apk_candidates:
        return apk_candidates[0]
    return None


def get_latest_local_apk():
    """Find the most recent APK from the local builds directory."""
    if not BUILDS_DIR.exists():
        return None

    apks = list(BUILDS_DIR.glob("build_*/*.apk"))
    if not apks:
        return None

    apks.sort(key=lambda x: x.stat().st_mtime, reverse=True)
    return apks[0]


def flash_apk_to_phone(apk_file):
    """Install the APK to connected Android device via ADB."""
    if not apk_file or not apk_file.exists():
        print(f"{RED}[ERROR] No APK file found to install!{RESET}")
        return False

    devices = check_adb_devices()
    if not devices:
        print(f"\n{RED}[!] No Android phone detected.{RESET}")
        print(f"    1. Connect your Samsung F23 via USB.")
        print(f"    2. Unlock phone & tap 'Allow USB debugging'.")
        print(f"    3. Press [4] in the menu to verify connection.\n")
        return False

    # Check for unauthorized
    if any("unauthorized" in d for d in devices):
        print(f"{YELLOW}[!] Phone is connected but 'UNAUTHORIZED'.{RESET}")
        print(f"    Look at your Samsung F23 screen now and tap 'ALLOW USB Debugging'.\n")
        return False

    print(f"\n{CYAN}Connected Device:{RESET} {devices[0]}")
    print(f"{CYAN}Flashing {apk_file.name} to phone...{RESET}")

    install_cmd = [str(ADB_PATH), "install", "-r", str(apk_file)]
    res = subprocess.run(install_cmd, capture_output=True, text=True)

    # Check for signature mismatch (INSTALL_FAILED_UPDATE_INCOMPATIBLE)
    combined_output = (res.stdout or "") + (res.stderr or "")
    if "INSTALL_FAILED_UPDATE_INCOMPATIBLE" in combined_output:
        print(f"{YELLOW}[!] Signature mismatch from previous build. Performing clean reinstall...{RESET}")
        subprocess.run([str(ADB_PATH), "uninstall", "com.minimalphone.launcher"], capture_output=True)
        res = subprocess.run([str(ADB_PATH), "install", str(apk_file)], capture_output=True, text=True)

    combined_output = (res.stdout or "") + (res.stderr or "")
    if "Success" in combined_output:
        print(f"\n{GREEN}{BOLD}🎉 SUCCESS: App installed on phone!{RESET}")
        print(f"{CYAN}Launching MiniMalPhone on your screen...{RESET}")
        launch_cmd = [str(ADB_PATH), "shell", "am", "start", "-n", "com.minimalphone.launcher/.MainActivity"]
        subprocess.run(launch_cmd, capture_output=True, text=True)
        return True
    else:
        print(f"{RED}Installation error: {combined_output.strip()}{RESET}")
        return False


def main_menu():
    while True:
        print_banner()
        print("Choose an action:")
        print(f"  {BOLD}[1]{RESET} Download Latest Build from GitHub (Unzip & Rotate 3 Folders)")
        print(f"  {BOLD}[2]{RESET} Flash Local APK to Phone (Samsung F23)")
        print(f"  {BOLD}[3]{RESET} 🚀 Fast-Track: Download Latest & Flash to Phone")
        print(f"  {BOLD}[4]{RESET} 🔍 Check Connected ADB Devices")
        print(f"  {BOLD}[5]{RESET} 🌐 Open GitHub Actions Build Page")
        print(f"  {BOLD}[0]{RESET} Exit")
        print()

        choice = input(f"{BOLD}Enter choice (0-5): {RESET}").strip()

        if choice == "1":
            apk = download_latest_build()
            if apk:
                print(f"\n{GREEN}Build ready at: {apk}{RESET}")
            input(f"\nPress Enter to return to menu...")

        elif choice == "2":
            apk = get_latest_local_apk()
            if apk:
                print(f"\nUsing local build: {apk}")
                flash_apk_to_phone(apk)
            else:
                print(f"{YELLOW}No local APK found. Please use [1] or [3] to download first.{RESET}")
            input(f"\nPress Enter to return to menu...")

        elif choice == "3":
            print(f"\n{CYAN}--- Step 1: Downloading & Unzipping Build ---{RESET}")
            apk = download_latest_build()
            if apk:
                print(f"\n{CYAN}--- Step 2: Flashing to Phone ---{RESET}")
                flash_apk_to_phone(apk)
            else:
                print(f"{RED}Could not fetch build. Check if GitHub Actions is still compiling.{RESET}")
            input(f"\nPress Enter to return to menu...")

        elif choice == "4":
            print(f"\n{CYAN}Checking ADB devices...{RESET}")
            devices = check_adb_devices()
            if devices:
                print(f"{GREEN}Detected {len(devices)} device(s):{RESET}")
                for d in devices:
                    print(f"  • {d}")
            else:
                print(f"{YELLOW}No devices detected. Plug in your phone via USB with USB Debugging enabled.{RESET}")
            input(f"\nPress Enter to return to menu...")

        elif choice == "5":
            import webbrowser
            print(f"\nOpening {ACTIONS_URL} in browser...")
            webbrowser.open(ACTIONS_URL)
            time.sleep(1)

        elif choice == "0":
            print(f"\n{CYAN}Happy minimalist coding! Goodbye.{RESET}")
            break


if __name__ == "__main__":
    main_menu()
