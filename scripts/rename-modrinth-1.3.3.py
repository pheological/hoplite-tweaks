"""Apply the owner's requested display title to the four existing 1.3.3 releases."""
import json
import os
import urllib.request

VERSIONS = {
    "EnY42Vj1": "1.21.11",
    "VxsZ2ojL": "26.1.2",
    "nmv6YA2d": "26.2",
    "NKcsGB0C": "26.3",
}


def request(version_id, data=None):
    headers = {
        "Authorization": os.environ["PUB_MODRINTH_TOKEN"],
        "User-Agent": "pheological/hoplite-tweaks release-title-maintenance",
        "Content-Type": "application/json",
    }
    req = urllib.request.Request(
        "https://api.modrinth.com/v2/version/" + version_id,
        headers=headers,
        data=json.dumps(data).encode() if data else None,
        method="PATCH" if data else "GET",
    )
    with urllib.request.urlopen(req, timeout=60) as response:
        body = response.read()
        return json.loads(body) if body else None


def main():
    original = {version_id: request(version_id) for version_id in VERSIONS}
    for version_id, version in original.items():
        if version["project_id"] != "gUBYoUQa" or version["version_number"] != "1.3.3-fabric+" + VERSIONS[version_id]:
            raise RuntimeError("Release identity mismatch: " + version_id)
    for version_id, version in original.items():
        request(version_id, {"name": "1.3.3"})
        updated = request(version_id)
        if updated["name"] != "1.3.3":
            raise RuntimeError("Title verification failed: " + version_id)
        for field in ("version_number", "game_versions", "loaders", "files"):
            if updated[field] != version[field]:
                raise RuntimeError("Unexpected change to " + field)
        print("Renamed and verified:", version_id, VERSIONS[version_id], "1.3.3", flush=True)


if __name__ == "__main__":
    main()
