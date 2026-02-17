# App Icon Generation Prompts — Ben's Bible

Use these prompts with DALL-E 3, Midjourney, or similar AI image generators.
Generate at **1024x1024 px**, PNG format.

---

## Prompt 1: Open Book with Luminous Cross

```
A modern minimal app icon design, 1024x1024 pixels, rounded square shape. An open book viewed from a slight angle with a luminous golden cross rising from the center of the pages, emitting a soft warm glow. The book is rendered in clean geometric shapes with warm brown tones (#5D4037, #4E342E). Background is a rich deep navy (#1A237E) fading to warm indigo (#4A148C). The cross glows in warm gold (#FFCC80, #FFB74D). Flat vector illustration style, ultra-clean lines, no texture, no shadows, no text, no words, no letters. Suitable for iOS and Android app icon. Matte finish, professional, spiritual, elegant.
```

**Style:** Warm glow on dark background — dramatic, eye-catching at small sizes
**Best for:** Standing out on a home screen with strong contrast

---

## Prompt 2: Book Silhouette with Cross Cutout

```
A modern minimal app icon, 1024x1024 pixels, rounded square format. A solid book silhouette in rich dark brown (#4E342E) centered on a warm beige (#EFEBE9) background. The cross is a negative-space cutout in the center of the book cover, revealing a warm golden gradient (#FFCC80 to #FFB74D) beneath. The book has a subtle spine line on the left edge. Ultra-clean flat design, no texture, no photorealism, no text, no words, no letters, no clutter. Vector illustration style, single-weight lines, matte finish. Professional spiritual app icon design.
```

**Style:** Negative-space design — elegant, understated, works at all sizes
**Best for:** Clean, recognizable silhouette that reads well as a tiny icon

---

## Prompt 3: Geometric Cross-Bookmark

```
A modern minimal app icon, 1024x1024 pixels, rounded square shape. A closed book shown from the front in warm brown (#5D4037) with a cross-shaped bookmark ribbon in gold (#FFCC80) extending from the top of the book. The background is a soft warm cream (#EFEBE9). The book and cross are rendered as simple geometric shapes with clean edges. Flat vector style, no gradients, no texture, no photorealism, no text, no words, no letters. Ultra-minimal, professional, matte finish. Suitable as a mobile app icon for a Bible reading application.
```

**Style:** Simple, iconic, single accent color — most minimal of the three
**Best for:** Maximum simplicity and readability at any size

---

## Tips for Best Results

- **In Midjourney**, append: `--style raw --ar 1:1 --s 50` for cleaner output
- **In DALL-E 3**, use "Natural" style (not "Vivid") for more controlled results
- After generation, you may want to:
  - Clean up in Figma/Photoshop to ensure perfect symmetry
  - Add the rounded-rect mask (iOS does this automatically; Android needs it in the adaptive icon setup)
  - Test at 29x29, 60x60, and 180x180 to ensure it reads well at small sizes

---

## Integration Instructions

### iOS
1. Export your final icon as `app-icon.png` (1024x1024, no transparency, no rounded corners — iOS adds them)
2. Place it in: `BensBible2/Assets.xcassets/AppIcon.appiconset/app-icon.png`
3. The `Contents.json` has already been updated to reference this file

### Android
1. The current icon uses vector XML drawables at `android/app/src/main/res/drawable/`
2. **Option A (recommended):** Update the vector XMLs to match your new design
3. **Option B:** Generate raster PNGs and place in `mipmap-mdpi/` through `mipmap-xxxhdpi/`
4. The adaptive icon config is at `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
