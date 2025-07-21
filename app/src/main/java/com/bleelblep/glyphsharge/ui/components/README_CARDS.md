# Standardized Card Templates for GlyphZen

This document provides guidelines for using the standardized card templates in the GlyphZen app.
These templates ensure visual consistency and maintainable code across the application.

## Available Card Templates

### 1. StandardCard (Base Template)

The main template that all other cards extend from. Provides maximum flexibility with all possible
parameters.

```kotlin
StandardCard(
    modifier = Modifier,
    title = "Card Title",
    subtitle = "Optional subtitle",
    description = "Optional description text",
    icon = Icons.Default.Star,
    actionText = "Action Button",
    onCardClick = { /* Handle card tap */ },
    onActionClick = { /* Handle button tap */ },
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    contentPadding = PaddingValues(16.dp)
) {
    // Custom content goes here
}
```

### 2. SimpleCard

For basic cards with minimal content.

```kotlin
SimpleCard(
    title = "Simple Title",
    description = "Optional description",
    onClick = { /* Handle click */ }
) {
    // Optional custom content
}
```

### 3. IconCard

For cards that need prominent icon display.

```kotlin
IconCard(
    title = "Settings",
    subtitle = "Configure preferences",
    icon = Icons.Default.Settings,
    actionText = "Open",
    onCardClick = { /* Handle card click */ },
    onActionClick = { /* Handle action */ }
)
```

### 4. ActionCard

For cards with prominent call-to-action buttons.

```kotlin
ActionCard(
    title = "Start Session",
    description = "Begin your breathing exercise",
    actionText = "Start Now",
    onActionClick = { /* Handle action */ }
)
```

### 5. ContentCard

For cards with primarily custom content.

```kotlin
ContentCard(
    title = "Custom Layout",
    contentPadding = PaddingValues(16.dp),
    onClick = { /* Optional click handler */ }
) {
    // Your custom composable content
    Text("Custom content here")
    Button(onClick = {}) { Text("Custom Button") }
}
```

## Design Guidelines

### Visual Consistency

- All cards use `MaterialTheme.shapes.large` (24.dp corner radius)
- Standard elevation: 4.dp
- Default padding: 16.dp
- Icons are 24.dp size with primary color tint

### Typography Hierarchy

- **Title**: `MaterialTheme.typography.titleLarge`
- **Subtitle**: `MaterialTheme.typography.titleMedium`
- **Description**: `MaterialTheme.typography.bodyMedium`
- **Action Button**: `MaterialTheme.typography.titleMedium`

### Color Usage

- **Card Background**: `MaterialTheme.colorScheme.surface`
- **Card Content**: `MaterialTheme.colorScheme.onSurface`
- **Icons**: `MaterialTheme.colorScheme.primary`
- **Subtitle/Description**: `MaterialTheme.colorScheme.onSurfaceVariant`

### Spacing

- Vertical spacing between elements: 8.dp
- Icon-to-text spacing: 12.dp
- Action button top margin: Additional 8.dp

## Usage Patterns

### Information Display Cards

Use `SimpleCard` or `IconCard` for displaying read-only information:

```kotlin
SimpleCard(
    title = "Session Complete",
    description = "You've completed a 5-minute breathing session"
)
```

### Interactive Feature Cards

Use `ActionCard` for features that require user action:

```kotlin
ActionCard(
    title = "Glyph Patterns",
    description = "Create custom light patterns",
    actionText = "Customize",
    onActionClick = { navigateToGlyphCustomizer() }
)
```

### Complex Layout Cards

Use `ContentCard` or `StandardCard` for custom layouts:

```kotlin
ContentCard(title = "Statistics") {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatColumn("Sessions", "42")
        StatColumn("Minutes", "1,250")
        StatColumn("Streak", "7 days")
    }
}
```

### Settings/Configuration Cards

Use `IconCard` with appropriate icons:

```kotlin
IconCard(
    title = "Notifications",
    subtitle = "Manage your alerts",
    icon = Icons.Default.Notifications,
    actionText = "Configure",
    onActionClick = { openNotificationSettings() }
)
```

## Best Practices

### Do's

✅ Use appropriate card variants for specific use cases
✅ Maintain consistent padding and spacing
✅ Use semantic icons that relate to the content
✅ Keep titles concise (1 line max)
✅ Limit descriptions to 3 lines max
✅ Use the theme's color scheme

### Don'ts

❌ Don't mix different card styles in the same context
❌ Don't override theme colors unnecessarily
❌ Don't use overly long text that breaks the layout
❌ Don't add too many interactive elements in one card
❌ Don't ignore the established spacing guidelines

## Accessibility

- All cards support click interactions when `onClick` is provided
- Icons include appropriate `contentDescription` values
- Text uses theme-appropriate contrast ratios
- Cards support semantic navigation

## Migration from Existing Cards

If you have existing custom cards, consider:

1. **Evaluate functionality**: Which template best fits your use case?
2. **Extract custom content**: Move unique content to the `content` lambda
3. **Update styling**: Remove custom colors/typography in favor of theme values
4. **Test interactions**: Ensure click handlers work as expected

## Examples

See `CardExamples.kt` for comprehensive usage examples and visual references.

## Support

For questions about the card template system or requests for new variants, contact the UI/UX team. 