"""Delete only the exact Modrinth releases approved in the checked-in manifest."""
import argparse
import json
import os
import time
from pathlib import Path
import urllib.error
import urllib.request

API = "https://api.modrinth.com/v2"
MANIFEST = Path(__file__).resolve().parents[1] / ".github/maintenance/withdraw-older-modrinth-versions.json"


def request(path, method="GET", token=None, data=None):
    headers = {"User-Agent": "pheological/hoplite-tweaks release-withdrawal"}
    if token:
        headers["Authorization"] = token
    body = json.dumps(data).encode() if data is not None else None
    if body is not None:
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(API + path, headers=headers, method=method, data=body)
    try:
        with urllib.request.urlopen(req, timeout=60) as response:
            body = response.read()
            return response.status, json.loads(body) if body else None
    except urllib.error.HTTPError as error:
        # Never print request headers, credentials, or response bodies.
        if error.code == 404:
            return 404, None
        raise RuntimeError(f"Modrinth {method} {path} returned HTTP {error.code}") from None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--delete", action="store_true", help="Execute the owner-approved deletion")
    args = parser.parse_args()
    manifest = json.loads(MANIFEST.read_text())
    project = manifest["project_id"]
    if project != "gUBYoUQa":
        raise RuntimeError("Unexpected project")
    versions = manifest["versions"]
    if len(versions) != 47 or len({v['id'] for v in versions}) != 47:
        raise RuntimeError("Unexpected deletion manifest")
    token = os.environ.get("PUB_MODRINTH_TOKEN") if args.delete else None
    if args.delete and not token:
        raise RuntimeError("GitHub Actions Modrinth secret is unavailable")

    # Validate the complete selection before making any changes. Missing releases
    # are already withdrawn, allowing safe recovery from a partial failure.
    pending = []
    for selected in versions:
        status, current = request("/version/" + selected["id"], token=token)
        if status == 404:
            print("Already absent:", selected["id"])
            continue
        if current["project_id"] != project or current["version_number"] != selected["version_number"]:
            raise RuntimeError("Release identity mismatch: " + selected["id"])
        if tuple(map(int, current["version_number"].split('.'))) >= (1, 3, 2):
            raise RuntimeError("Refusing to delete a fixed release")
        pending.append(selected)
    print(f"Validated {len(pending)} existing older releases; fixed releases are excluded.")
    if not args.delete:
        print("Read-only validation complete; no releases deleted.")
        return
    failures = []
    for selected in pending:
        path = "/version/" + selected["id"]
        try:
            status, _ = request(path, method="DELETE", token=token)
            if status not in (204, 404):
                raise RuntimeError("Unexpected deletion status: " + str(status))
            for attempt in range(3):
                status, _ = request(path, token=token)
                if status == 404:
                    break
                time.sleep(1)
            if status != 404:
                raise RuntimeError("Deletion verification failed: " + selected["id"])
            print("Deleted and verified absent:", selected["id"], selected["version_number"], flush=True)
        except RuntimeError as error:
            # Continue withdrawing other releases even if Modrinth cannot delete one.
            failures.append(selected["id"])
            print(str(error), flush=True)
            try:
                request(path, method="PATCH", token=token, data={"status": "draft"})
                _, current = request(path, token=token)
                if current and current.get("status") == "draft":
                    print("Deletion blocked; hidden as draft:", selected["id"], flush=True)
                else:
                    print("Could not verify draft status:", selected["id"], flush=True)
            except RuntimeError as hide_error:
                print(str(hide_error), flush=True)
    _, remaining = request("/project/" + project + "/version", token=token)
    if {v["id"] for v in versions} & {v["id"] for v in remaining}:
        raise RuntimeError("An approved older release is still listed")
    print(f"Withdrawal verified. Remaining public project releases: {len(remaining)}")
    if failures:
        raise RuntimeError("Permanent deletion still needs verification for: " + ", ".join(failures))


if __name__ == "__main__":
    main()
