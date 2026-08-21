// Renders the EasyRides brand mark directly into the already-generated
// android/ project's per-density mipmap folders, bypassing `expo prebuild`
// (which would also regenerate gradle-wrapper.properties/gradle.properties/
// build.gradle and wipe out the manual native-build fixes already applied
// to get this project compiling on this machine's JDK 25 / no-JDK-17 setup).
const Jimp = require('jimp-compact');
const path = require('path');
const fs = require('fs');

const BLUE = { r: 0x0b, g: 0x5f, b: 0xbb };
const TEAL = { r: 0x00, g: 0x79, b: 0x6c };
const WHITE = { r: 0xff, g: 0xff, b: 0xff };

function lerp(a, b, t) {
  return Math.round(a + (b - a) * t);
}
function gradientColor(x, y, size) {
  const t = Math.max(0, Math.min(1, (x + y) / (2 * size)));
  return { r: lerp(BLUE.r, TEAL.r, t), g: lerp(BLUE.g, TEAL.g, t), b: lerp(BLUE.b, TEAL.b, t) };
}
function withinCircle(x, y, size) {
  const cx = size / 2, cy = size / 2, r = size / 2;
  const dx = x - cx, dy = y - cy;
  return dx * dx + dy * dy <= r * r;
}
function eRects(size, scale) {
  const w = size * scale, h = size * scale;
  const x0 = (size - w) / 2, y0 = (size - h) / 2;
  const stroke = w * 0.28;
  return [
    { x: x0, y: y0, w: stroke, h },
    { x: x0, y: y0, w, h: stroke },
    { x: x0, y: y0 + h / 2 - stroke / 2, w: w * 0.82, h: stroke },
    { x: x0, y: y0 + h - stroke, w, h: stroke },
  ];
}
function pointInRects(x, y, rects) {
  return rects.some((r) => x >= r.x && x < r.x + r.w && y >= r.y && y < r.y + r.h);
}

function makeBackground(size, circular) {
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
function makeForeground(size, scale) {
  const img = new Jimp(size, size, 0x00000000);
  const rects = eRects(size, scale);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      if (pointInRects(x, y, rects)) {
        img.setPixelColor(Jimp.rgbaToInt(WHITE.r, WHITE.g, WHITE.b, 255), x, y);
      }
    }
  }
  return img;
}

// dp -> px per density bucket
const densities = { mdpi: 1, hdpi: 1.5, xhdpi: 2, xxhdpi: 3, xxxhdpi: 4 };
const ADAPTIVE_DP = 108; // background/foreground/monochrome layer canvas
const LEGACY_DP = 48; // flattened ic_launcher/ic_launcher_round

async function main() {
  const resDir = path.join(__dirname, '..', 'android', 'app', 'src', 'main', 'res');

  // Master renders at high res, resized down per bucket (cleaner than
  // rendering the vector math fresh at tiny sizes).
  const masterBg = makeBackground(1024, false);
  const masterFg = makeForeground(1024, 0.42);
  const masterLegacyBg = makeBackground(1024, false);
  const masterLegacyFg = makeForeground(1024, 0.5);
  const masterRoundBg = makeBackground(1024, true);

  for (const [bucket, mult] of Object.entries(densities)) {
    const dir = path.join(resDir, `mipmap-${bucket}`);
    fs.mkdirSync(dir, { recursive: true });

    const adaptivePx = Math.round(ADAPTIVE_DP * mult);
    const legacyPx = Math.round(LEGACY_DP * mult);

    // Remove the old webp files; PNGs with the same base resource name work identically.
    for (const name of ['ic_launcher', 'ic_launcher_round', 'ic_launcher_background', 'ic_launcher_foreground', 'ic_launcher_monochrome']) {
      const webp = path.join(dir, `${name}.webp`);
      if (fs.existsSync(webp)) fs.unlinkSync(webp);
    }

    await masterBg.clone().resize(adaptivePx, adaptivePx).writeAsync(path.join(dir, 'ic_launcher_background.png'));
    await masterFg.clone().resize(adaptivePx, adaptivePx).writeAsync(path.join(dir, 'ic_launcher_foreground.png'));
    await masterFg.clone().resize(adaptivePx, adaptivePx).writeAsync(path.join(dir, 'ic_launcher_monochrome.png'));

    // Legacy flattened square icon (pre-Android-8 launchers).
    const legacySquare = masterLegacyBg.clone().resize(legacyPx, legacyPx);
    const legacyGlyph = masterLegacyFg.clone().resize(legacyPx, legacyPx);
    legacySquare.composite(legacyGlyph, 0, 0);
    await legacySquare.writeAsync(path.join(dir, 'ic_launcher.png'));

    // Legacy round icon, same but clipped to a circle.
    const roundBg = masterRoundBg.clone().resize(legacyPx, legacyPx);
    const roundGlyph = masterLegacyFg.clone().resize(legacyPx, legacyPx);
    roundBg.composite(roundGlyph, 0, 0);
    await roundBg.writeAsync(path.join(dir, 'ic_launcher_round.png'));
  }

  console.log('Regenerated Android launcher icon resources in', resDir);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
