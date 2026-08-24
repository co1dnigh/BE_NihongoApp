# -*- coding: utf-8 -*-
"""
Tao toan bo file anh + am thanh cho bo du lieu demo, doc danh sach tu out/media.json.

Anh   : PNG 512x512, hinh minh hoa vector (emoji mau cua Windows) tren nen gradient pastel.
        Moi tu 1 anh, mau nen suy ra tu ten file nen chay lai luon ra ket qua giong het.
Am thanh: MP3 sinh boi edge-tts (giong neural that cua Microsoft), giong nu ja-JP-NanamiNeural,
        toc do cham hon 10% cho nguoi moi bat dau nghe ro tung am.

Chay: python make_media.py            (bo qua file da co)
      python make_media.py --force    (tao lai tu dau)
"""
import asyncio
import colorsys
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFont

BASE = os.path.dirname(os.path.abspath(__file__))
UPLOADS = os.path.abspath(os.path.join(BASE, "..", "..", "uploads"))
EMOJI_FONT = "C:/Windows/Fonts/seguiemj.ttf"
SIZE = 512
FORCE = "--force" in sys.argv


# ---------------------------------------------------------------- ẢNH
def palette(seed_text):
    """Màu nền pastel ổn định theo tên file: cùng tên -> cùng màu ở mọi lần chạy."""
    h = (sum((i + 1) * ord(c) for i, c in enumerate(seed_text)) % 360) / 360.0
    top = colorsys.hsv_to_rgb(h, 0.18, 1.00)
    bottom = colorsys.hsv_to_rgb((h + 0.06) % 1.0, 0.42, 0.96)
    to255 = lambda c: tuple(int(x * 255) for x in c)  # noqa: E731
    return to255(top), to255(bottom)


def make_image(path, emoji, seed_text):
    top, bottom = palette(seed_text)
    img = Image.new("RGB", (SIZE, SIZE), top)
    d = ImageDraw.Draw(img)
    for y in range(SIZE):  # gradient dọc
        t = y / (SIZE - 1)
        d.line([(0, y), (SIZE, y)],
               fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))

    # đĩa trắng mờ phía sau cho hình nổi bật trên mọi tông nền
    disc = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    ImageDraw.Draw(disc).ellipse([SIZE * 0.12, SIZE * 0.12, SIZE * 0.88, SIZE * 0.88],
                                 fill=(255, 255, 255, 105))
    img = Image.alpha_composite(img.convert("RGBA"), disc)

    layer = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    font = ImageFont.truetype(EMOJI_FONT, 270)
    ImageDraw.Draw(layer).text((SIZE // 2, SIZE // 2 + 6), emoji, font=font,
                               anchor="mm", embedded_color=True)
    img = Image.alpha_composite(img, layer)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.convert("RGB").save(path, "PNG", optimize=True)


def build_images(items):
    made = skipped = 0
    for it in items:
        path = os.path.join(UPLOADS, "images", it["rel"].replace("/", os.sep))
        if os.path.exists(path) and not FORCE:
            skipped += 1
            continue
        make_image(path, it["emoji"], it["rel"])
        made += 1
        if made % 50 == 0:
            print("   ... %d anh" % made, flush=True)
    print("ANH: tao moi %d, bo qua %d (da co)" % (made, skipped))


# ---------------------------------------------------------------- ÂM THANH
async def build_audio(items):
    import edge_tts

    todo = []
    for it in items:
        path = os.path.join(UPLOADS, "audios", it["rel"].replace("/", os.sep))
        if os.path.exists(path) and os.path.getsize(path) > 1000 and not FORCE:
            continue
        os.makedirs(os.path.dirname(path), exist_ok=True)
        todo.append((it, path))
    print("AM THANH: can tao %d / %d file" % (len(todo), len(items)), flush=True)

    sem = asyncio.Semaphore(6)
    done = [0]
    failed = []

    async def one(it, path):
        async with sem:
            for attempt in range(3):
                try:
                    c = edge_tts.Communicate(it["text"], it["voice"], rate="-10%")
                    await c.save(path)
                    if os.path.getsize(path) > 500:
                        done[0] += 1
                        if done[0] % 50 == 0:
                            print("   ... %d/%d" % (done[0], len(todo)), flush=True)
                        return
                except Exception as e:                       # noqa: BLE001
                    if attempt == 2:
                        failed.append((it["rel"], str(e)[:120]))
                    await asyncio.sleep(1.5 * (attempt + 1))

    await asyncio.gather(*[one(it, p) for it, p in todo])
    print("AM THANH: tao thanh cong %d, that bai %d" % (done[0], len(failed)))
    for rel, err in failed[:10]:
        print("   LOI %s: %s" % (rel, err))
    return failed


def main():
    with open(os.path.join(BASE, "out", "media.json"), encoding="utf-8") as f:
        media = json.load(f)
    print("Thu muc uploads: %s" % UPLOADS)
    build_images(media["image"])
    asyncio.run(build_audio(media["audio"]))


if __name__ == "__main__":
    main()
