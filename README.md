# Glyph Sharge

A powerful glyph interface management app for Nothing Phones that enhances the Glyph Interface capabilities with advanced features and customization options.

## Features

- **Glyph Service Control**: Comprehensive management of the Nothing Glyph Interface
- **Power Peek**: Quick battery status visualization through glyph patterns
- **Glyph Guard**: Security feature with customizable duration and sound alerts
- **Hardware Diagnostics**: Built-in tools for testing and verifying glyph functionality
- **Material 3 Design**: Modern UI with standardized card templates
- **Theme Customization**: Dark/Light theme support with custom font options
- **Adaptive UI**: Smart detection of Nothing Phone models with optimized glyph patterns

## Documentation

- [Glyph Implementation Guide](GLYPH_IMPLEMENTATION.md) - Detailed technical documentation of glyph interface implementation
- [Material 3 Implementation Guide](Material3_Expressive_Implementation_Guide.md) - Material 3 design system implementation
- [Card Examples Guide](Enhanced_Card_Examples_Guide.md) - Examples and usage of card components
- [Animation and Font Guide](Animation_And_Font_Fixes.md) - Animation system and typography documentation
- [Motion Layout Guide](MOTIONLAYOUT_ANIMATIONS.md) - Motion layout animations and transitions
- [Settings Readability Guide](Settings_Text_Readability_Fixes.md) - Settings UI and text improvements
- [Text Readability Guide](Text_Readability_Improvements.md) - Typography and text optimization
- [Wavy Progress Guide](WavyProgressIndicator_Usage_Guide.md) - Custom progress indicator implementation

## Confirmed Working & Supported Phone Models

- Nothing Phone (2)
- Nothing Phone (2a) Plus
- Nothing Phone (3a)
  
Each phone model's unique glyph patterns are automatically detected and optimized.

## Core Components

### Glyph Management
- `GlyphManager`: Core interface to the Nothing Glyph SDK
- `GlyphAnimationManager`: Handles complex animation patterns
- `GlyphController`: Direct channel control interface

### Features
- **Power Peek**: Battery status visualization
- **Glyph Guard**: Security monitoring with customizable alerts
- **Hardware Diagnostics**: Comprehensive testing tools
- **Vibration Settings**: Customizable haptic feedback
- **Theme Settings**: UI customization options

## Technical Implementation

- Built with Jetpack Compose and Material 3
- Uses Hilt for dependency injection
- Implements MVVM architecture
- Optimized for Android 14+ (API 34)
- Background service support for persistent features

## Development Setup

1. Place the Nothing Glyph SDK JAR files in the `app/libs` folder
2. Enable Glyph Interface debug mode on your Nothing Phone:
   ```bash
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
3. The app uses the "test" API key in debug mode (configured in AndroidManifest.xml)

## UI Components

The app features a comprehensive card template system:
- `StandardCard`: Base template with maximum flexibility
- `SimpleCard`: Basic cards with minimal content
- `IconCard`: Cards with prominent icon display
- `ActionCard`: Cards with prominent call-to-action buttons
- `ContentCard`: Cards with custom content layouts

## Version Information

- Current Version: 1.0.6-GlyphRewrite
- Minimum SDK: 34 (Android 14+)
- Target SDK: 34
- Build Tools: Latest Android Studio

## Credits

Developed for Nothing Phone users to enhance their Glyph Interface experience. 
