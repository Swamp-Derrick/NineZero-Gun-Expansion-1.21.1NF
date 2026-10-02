"""Validate NZGE resources and references against the exact supplied CGM jar."""
from pathlib import Path
import json
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
CGM = zipfile.ZipFile(ROOT / "libs/cgm-1.4.4.jar")
errors = []


def exists(location, category, suffix):
    namespace, name = location.split(":", 1)
    path = f"assets/{namespace}/{category}/{name}{suffix}"
    if namespace == "minecraft":
        return True  # Vanilla references are checked by the actual client model bake.
    return (RES / path).is_file() or path in CGM.namelist()


json_count = 0
for file in RES.rglob("*"):
    if file.suffix not in (".json", ".cgmmeta", ".mcmeta"):
        continue
    data = json.loads(file.read_text(encoding="utf-8"))
    json_count += 1
    if "/models/" not in file.as_posix():
        continue
    for texture in data.get("textures", {}).values():
        if not texture.startswith("#") and not exists(texture, "textures", ".png"):
            errors.append(f"{file.relative_to(ROOT)}: unresolved texture {texture}")
    for override in data.get("overrides", []):
        if not exists(override["model"], "models", ".json"):
            errors.append(f"{file.relative_to(ROOT)}: unresolved model {override['model']}")
    parent = data.get("parent")
    if parent and ":" in parent and not exists(parent, "models", ".json"):
        errors.append(f"{file.relative_to(ROOT)}: unresolved parent {parent}")
    if not parent:
        for part in data.get("components", data.get("elements", [])):
            for face in part.get("faces", {}).values():
                texture = face.get("texture", "")
                if texture.startswith("#") and texture[1:] not in data.get("textures", {}):
                    errors.append(f"{file.relative_to(ROOT)}: undefined texture variable {texture}")

items_source = (ROOT / "src/main/java/zaeonninezero/nzgmaddon/init/initItems.java").read_text()
items = re.findall(r'ITEMS.register\("([^"]+)"', items_source)
guns = re.findall(r'ITEMS.register\("([^"]+)".*?new GunItem', items_source)
assert len(items) == 41 and len(guns) == 21
assert set(guns) == {p.stem for p in (RES / "data/nzgmaddon/guns").glob("*.json")}
recipes = list((RES / "data/nzgmaddon/recipe").glob("*.json"))
assert len(recipes) == 52
for file in recipes:
    recipe = json.loads(file.read_text())
    assert recipe["type"] == "nzgmaddon:workbench"
    assert recipe["recipe_id"] == f"nzgmaddon:{file.stem}"
    assert recipe["result"]["id"].split(":")[1] in items
    assert "nbt" not in recipe["result"]
    assert all(not material.get("tag", "").startswith("forge:") for material in recipe["materials"])

sounds = json.loads((RES / "assets/nzgmaddon/sounds.json").read_text())
sound_source = (ROOT / "src/main/java/zaeonninezero/nzgmaddon/init/initSounds.java").read_text()
for event in re.findall(r'= register\("([^"]+)"', sound_source):
    assert event in sounds, f"No sound definition for registered event {event}"
for event, definition in sounds.items():
    for sound in definition.get("sounds", []):
        location = sound if isinstance(sound, str) else sound["name"]
        if isinstance(sound, dict) and sound.get("type") == "event":
            continue
        if not exists(location, "sounds", ".ogg"):
            errors.append(f"{event}: unresolved sound {location}")

if errors:
    raise SystemExit("\n".join(errors))
print(f"PASS: {json_count} JSON/metadata files; 41 items; 21 guns; 52 recipes; all model, texture and sound references resolved.")
