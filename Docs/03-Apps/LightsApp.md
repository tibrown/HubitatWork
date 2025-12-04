# LightsApp

## Overview
LightsApp is a comprehensive lighting automation app that controls various lights and light strips based on Hubitat modes, motion detection, and button presses. It manages desk lighting, decorative light strips, and general switches/outlets throughout the home.

## Features
- **Mode-based lighting control** for Night, Evening, Morning, and Day modes
- **Desk light automation** with motion and button control
- **Color-changing light strips** with different colors and brightness for each mode
- **Conditional automation** based on PTO and Holiday status
- **Centralized control** of multiple switches and outlets

## Setup in Hubitat Hub

### Installation
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `LightsApp.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **LightsApp**

### Configuration Options

#### Desk Control Section
- **Desk Motion** (optional): Motion sensor to trigger dim desk lighting
- **Desk Button** (optional): Pushable button device for desk light control
  - Single press (Button 1): Sets desk light to 100% brightness
  - Double tap (Button 1): Sets desk light to 5% brightness
- **Desk Light (CT)** (optional): Color temperature capable light for the desk
  - Automatically sets to Soft White (2700K)

#### Strips Section
- **Light Strip** (optional): Color-capable light strip for ambient lighting
- **Lan Strip** (optional): Secondary color-capable light strip

#### Switches & Outlets Section
- **Switches & Outlets** (optional, multiple): General switches and outlets controlled by mode changes

#### Conditions Section
- **On PTO Switch** (optional): When ON, prevents Morning mode from turning on lights
- **Holiday Switch** (optional): When ON, prevents Morning mode from turning on lights

## How It Works

### Mode-Based Automation

#### Night Mode
- All configured switches turn OFF
- Light strips set to Blue at 30% brightness
- LAN strip set to Blue at 30% brightness
- Desk light automatically sets to dimmest (5%, Soft White)

#### Evening Mode
- All configured switches turn ON
- Light strip set to Soft White at 50% brightness
- LAN strip set to Yellow at 96% brightness
- Creates warm, welcoming atmosphere

#### Morning Mode
- **Conditions checked first:**
  - If Holiday switch is ON: Only light strip is set, other devices not affected
  - If On PTO switch is ON: Only light strip is set, other devices not affected
  - If both are OFF: All switches turn ON, both strips activate
- Light strip set to Soft White at 50% brightness
- LAN strip set to Yellow at 96% brightness (only if conditions are met)

#### Day Mode
- All configured switches turn OFF
- Light strip turns OFF
- LAN strip turns OFF
- Maximizes natural light usage

### Desk Light Control

The desk light has three control methods:

1. **Motion Detection**: When desk motion is detected, sets light to 5% Soft White (2700K)
2. **Button Single Press**: Sets light to 100% Soft White for working
3. **Button Double Tap**: Sets light to 5% Soft White for ambient lighting

All desk light operations use Soft White (2700K) color temperature for comfortable working conditions.

### Color Settings

The app uses specific color profiles:
- **Blue**: Hue 66, Saturation 100 (Night mode)
- **Soft White**: Color Temperature 2700K (Morning, Evening, Desk)
- **Yellow**: Hue 18, Saturation 19 (LAN strip in Morning/Evening)

## Troubleshooting

### Lights don't respond to mode changes
- Verify devices are selected in app configuration
- Check that devices are online and responsive
- Review logs for error messages

### Desk light not responding to button
- Ensure you're pressing Button 1
- Verify button device is properly paired
- Check that Desk Light (CT) device is selected

### LAN strip wrong color
- The LAN strip uses a custom yellow color in Morning/Evening modes
- Check device capabilities - must support color control

### Switches not turning on in Morning mode
- Verify Holiday and On PTO switches are both OFF
- Check mode is actually set to "Morning"
- Ensure switches are selected in configuration

## Tips
- Use virtual switches for Holiday and On PTO conditions
- Test each mode manually before relying on automated mode changes
- Light strips need 500ms delay when turning on before color commands
- All color-capable devices must support the `setColor` command
