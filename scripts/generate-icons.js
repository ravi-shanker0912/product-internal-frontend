// One-off icon generator: draws the EasyRides brand mark (blue->teal
// diagonal gradient circle, white block "E" monogram) using jimp-compact
// (already a transitive dependency of @expo/image-utils, so no native
// image libs like sharp/ImageMagick needed on this machine).
const Jimp = require('jimp-compact');
const path = require('path');

const BLUE = { r: 0x0b, g: 0x5f, b: 0xbb };
const TEAL = { r: 0x00, g: 0x79, b: 0x6c };
const WHITE = { r: 0xff, g: 0xff, b: 0xff };

function lerp(a, b, t) {
  return Math.round(a + (b - a) * t);
}

function gradientColor(x, y, size) {
  const t = Math.max(0, Math.min(1, (x + y) / (2 * size)));
  return {
    r: lerp(BLUE.r, TEAL.r, t),
    g: lerp(BLUE.g, TEAL.g, t),
    b: lerp(BLUE.b, TEAL.b, t),
  };
}

function withinCircle(x, y, size) {
  const cx = size / 2;
  const cy = size / 2;
  const r = size / 2;
  const dx = x - cx;
  const dy = y - cy;
  return dx * dx + dy * dy <= r * r;
}

// Block "E" bounding box as a fraction of canvas size, centered.
function eRects(size, scale) {
  const w = size * scale;
  const h = size * scale;
  const x0 = (size - w) / 2;
  const y0 = (size - h) / 2;
  const stroke = w * 0.28;
  return [
    { x: x0, y: y0, w: stroke, h }, // vertical bar
    { x: x0, y: y0, w, h: stroke }, // top bar
    { x: x0, y: y0 + h / 2 - stroke / 2, w: w * 0.82, h: stroke }, // middle bar
    { x: x0, y: y0 + h - stroke, w, h: stroke }, // bottom bar
  ];
}

function pointInRects(x, y, rects) {
  return rects.some((r) => x >= r.x && x < r.x + r.w && y >= r.y && y < r.y + r.h);
}

async function makeBackground(size, { circular }) {
  const img = new Jimp(size, size, 0x00000000);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      if (circular && !withinCircle(x, y, size)) continue;
      const c = gradientColor(x, y, size);
      img.setPixelColor(Jimp.rgbaToInt(c.r, c.g, c.b, 255), x, y);
    }
  }
  return img;
}

async function makeForeground(size, scale, { color, transparent }) {
  const img = new Jimp(size, size, transparent ? 0x00000000 : 0xffffffff);
  const rects = eRects(size, scale);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      if (pointInRects(x, y, rects)) {
        img.setPixelColor(Jimp.rgbaToInt(color.r, color.g, color.b, 255), x, y);
      }
    }
  }
  return img;
}

async function main() {
  const outDir = path.join(__dirname, '..', 'assets');

  // icon.png: flat square (no transparency -- iOS doesn't support
  // transparent app icons), gradient bg + white E, used as the base icon.
  const icon = await makeBackground(1024, { circular: false });
  const iconGlyph = await makeForeground(1024, 0.5, { color: WHITE, transparent: true });
  icon.composite(iconGlyph, 0, 0);
  await icon.writeAsync(path.join(outDir, 'icon.png'));

  // Android adaptive icon: separate background/foreground layers, safe
  // zone keeps foreground content within the center ~66% of the canvas.
  const androidBg = await makeBackground(1024, { circular: false });
  await androidBg.writeAsync(path.join(outDir, 'android-icon-background.png'));

  const androidFg = await makeForeground(1024, 0.42, { color: WHITE, transparent: true });
  await androidFg.writeAsync(path.join(outDir, 'android-icon-foreground.png'));

  // Monochrome layer for Android 13+ themed icons: single-color glyph
  // on transparent, the OS tints it to match the user's wallpaper theme.
  const androidMono = await makeForeground(1024, 0.42, { color: WHITE, transparent: true });
  await androidMono.writeAsync(path.join(outDir, 'android-icon-monochrome.png'));

  // Splash icon: shown centered on a plain background at launch --
  // reuse the transparent glyph so it sits cleanly on any bg color.
  const splash = await makeForeground(1024, 0.5, { color: BLUE, transparent: true });
  await splash.writeAsync(path.join(outDir, 'splash-icon.png'));

  // Favicon: small flat square version for the (unused) web target.
  const favicon = await makeBackground(196, { circular: false });
  const faviconGlyph = await makeForeground(196, 0.5, { color: WHITE, transparent: true });
  favicon.composite(faviconGlyph, 0, 0);
  await favicon.writeAsync(path.join(outDir, 'favicon.png'));

  console.log('Generated all icon assets in', outDir);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
