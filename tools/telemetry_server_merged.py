import argparse
import json
import math
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

TOKEN = "lab-token"
LOCK = threading.Lock()
FIELDS = ("id", "latitude", "longitude", "speedKmh", "timestamp", "isMocked")
RECORDS = [{"id": "tel_01", "latitude": 19.4326, "longitude": -99.1332,
            "speedKmh": 62.5, "timestamp": 1700000000000, "isMocked": True}]


class Handler(BaseHTTPRequestHandler):
    def reply(self, code, value):
        raw = json.dumps(value, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        try:
            self.wfile.write(raw)
        except (BrokenPipeError, ConnectionResetError):
            pass

    def ready(self):
        if urlparse(self.path).path != "/v1/telemetry":
            self.reply(404, {"message": "Ruta inexistente"})
            return False
        if self.headers.get("Authorization") != "Bearer " + TOKEN:
            self.reply(401, {"message": "Token incorrecto o ausente"})
            return False
        time.sleep(self.server.delay)
        if self.server.scenario == "error500":
            self.reply(500, {"message": "Error simulado"})
            return False
        return True

    def do_GET(self):
        if not self.ready():
            return
        if self.server.scenario == "invalid":
            self.reply(200, [{"id": "incompleto"}])
            return
        try:
            limit = int(parse_qs(urlparse(self.path).query).get("limit", ["50"])[0])
            if not 1 <= limit <= 100:
                raise ValueError()
        except ValueError:
            self.reply(400, {"message": "limit debe estar entre 1 y 100"})
            return
        with LOCK:
            records = [] if self.server.scenario == "empty" else list(RECORDS[:limit])
        self.reply(200, records)

    def do_POST(self):
        if not self.ready():
            return
        try:
            size = int(self.headers.get("Content-Length", "0"))
            if not 0 < size <= 65536:
                raise ValueError()
            data = json.loads(self.rfile.read(size))
            if not isinstance(data, dict) or any(key not in data for key in FIELDS):
                raise ValueError()
            if not isinstance(data["id"], str) or not data["id"].strip():
                raise ValueError()
            for key in ("latitude", "longitude", "speedKmh"):
                if type(data[key]) not in (int, float) or not math.isfinite(data[key]):
                    raise ValueError()
            if not -90 <= data["latitude"] <= 90 or not -180 <= data["longitude"] <= 180:
                raise ValueError()
            if data["speedKmh"] < 0 or type(data["timestamp"]) is not int or data["timestamp"] < 0:
                raise ValueError()
            if type(data["isMocked"]) is not bool:
                raise ValueError()
        except (ValueError, TypeError, UnicodeDecodeError):
            self.reply(400, {"message": "Payload incompatible"})
            return
        item = {key: data[key] for key in FIELDS}
        with LOCK:
            existing = next((record for record in RECORDS if record["id"] == item["id"]), None)
            if existing is not None:
                code, result = (200, existing) if existing == item else (409, {"message": "Id ya usado con otros datos"})
            else:
                RECORDS.insert(0, item)
                code, result = 201, item
        self.reply(code, result)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument("--delay", type=float, default=1.0)
    parser.add_argument("--scenario", choices=["normal", "empty", "error500", "invalid"], default="normal")
    args = parser.parse_args()
    server = ThreadingHTTPServer(("127.0.0.1", args.port), Handler)
    server.delay = max(0.0, args.delay)
    server.scenario = args.scenario
    print(f"API TelemetryData: http://127.0.0.1:{args.port}/v1/telemetry ({args.scenario})")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()