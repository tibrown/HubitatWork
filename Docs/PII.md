# PII Scan Findings

This document lists potential Personally Identifiable Information (PII) found in the repository.

## Summary

- **Names**: First names "Marji" and "Tim" were found in device names and variable names.
- **IP Addresses**: One private IP address (`192.168.0.43`) was found.
- **Secrets/Keys**: No active secrets or keys were found. (Examples in documentation were ignored).
- **Contact Info**: No email addresses or phone numbers were found.
- **Location**: No physical addresses or GPS coordinates were found.

## Detailed Findings

### Names

First names appear in device labels and variable names. This is generally low-risk but noted here.

*   **Marji**
    *   `Docs/devices-table.md`: Device "marjis light"
    *   `Apps/Night/Night-IntruderConcreteShed.txt`: Variable "MarjiPhoneHome", "MarjisPhoneAway"
    *   `Apps/Night/Night-IntruderWoodshed.txt`: Variable "MarjisPhoneHome"
    *   `Apps/Night/Plan.md`: Reference to "marjis light"
    *   `Apps/Night/Night-RPDBirdHouse.txt`: Reference to "marjis light"

*   **Tim**
    *   `Docs/devices-table.md`: Device "TimsPhoneHome", "TimsPhoneHomeHD"
    *   `Apps/Night/Night-RPDBirdHouse.txt`: Reference to "Tims MotoG"

### IP Addresses

Private IP addresses are generally safe to commit, but listed for completeness.

*   **192.168.0.43**
    *   `Apps/Night/Night-IntruderWoodshed.txt`: Found in `sourceIp` field for "IgnoreMainsCheck on Office(new)".
    *   `Apps/Night/Night-IntruderConcreteShed.txt`: Found in `sourceIp` field.

### False Positives (Documentation)

The following files contain keywords like "password" or "apiKey" but are part of code examples or documentation:

*   `Docs/07-Best-Practices/Best-Practices.md`
