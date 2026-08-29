# Android Emulator Optimization for AMD Ryzen (Windows)

This document describes the steps taken to optimize the Android Emulator on an AMD platform where standard Windows Hypervisor Platform (WHPX) features are unavailable or problematic.

## 1. Handling the "Failed to enable WHPX" Error
On certain Windows 10/11 builds (e.g., "Isolated Workstation"), the `HypervisorPlatform` feature may be missing from the OS image. Android Studio often triggers a "Failed to enable WHPX" dialog (Error `-2146498548`).

**Solution:**
If using an AMD CPU, use the **Android Emulator Hypervisor Driver (AEHD)** instead. If `emulator -accel-check` confirms `AEHD is installed and usable`, the WHPX error can be safely ignored.

## 2. Optimized Boot Configuration
To ensure AEHD has exclusive and stable access to hardware virtualization (SVM), disable the Windows hypervisor launch at startup.

**Command (Run as Administrator):**
```powershell
bcdedit /set hypervisorlaunchtype off
```
*Note: This may disable WSL2 or Hyper-V features if they were previously working.*

## 3. Fixing GPU Rendering (Forced Hardware GPU)
The emulator may fall back to software rendering (SwiftShader/lavapipe) even if a powerful GPU is present, causing lag.

**Manual Configuration:**
1. Locate the AVD configuration file (usually in `~/.android/avd/<Device_Name>.avd/config.ini`).
2. Ensure the following line is set to `host`:
   ```ini
   hw.gpu.mode=host
   ```
3. In Android Studio AVD Manager, ensure **Graphics** is set to **Hardware - GLES 2.0**.

## 4. Verification of Acceleration
To verify that both CPU and GPU acceleration are active:

### CPU Acceleration Check:
Run the following command from the emulator directory:
```powershell
.\emulator.exe -accel-check
```
**Expected Output:** `AEHD (version X.X) is installed and usable.`

### Full Initialization Check:
Run the emulator in verbose mode to inspect the log:
```powershell
.\emulator.exe -avd <Your_AVD_Name> -verbose -read-only -no-window
```
**Look for these lines in the output:**
- `CPU Acceleration status: AEHD (version 2.2) is installed and usable.`
- `AEHD is operational`
- `Selected Vulkan renderer: Host`
- `Graphics Adapter Android Emulator OpenGL ES Translator (AMD Radeon ...)`

---
*Last Updated: 2026-08-29*
