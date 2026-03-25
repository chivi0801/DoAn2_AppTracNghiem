import io
import sys
import tempfile
from pathlib import Path

import cv2
from flask import Flask, jsonify, request, send_file

BASE_DIR = Path(__file__).resolve().parents[1]
DO_AN1_DIR = BASE_DIR / "DO_AN1"

if str(DO_AN1_DIR) not in sys.path:
    sys.path.insert(0, str(DO_AN1_DIR))

from test import XuLyAnh

app = Flask(__name__)


@app.route("/scan")
def scan():
    return "Ket noi thanh cong voi Flask"


@app.route("/predict", methods=["POST"])
def predict():
    file = request.files.get("file")
    if file is None or file.filename == "":
        return jsonify({"error": 'Vui long gui file anh voi key "file".'}), 400

    suffix = Path(file.filename).suffix or ".jpg"

    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_input:
        file.save(temp_input.name)
        temp_input_path = temp_input.name

    try:
        output_image = XuLyAnh(temp_input_path)
        ok, encoded_image = cv2.imencode(".jpg", output_image)
        if not ok:
            return jsonify({"error": "Khong ma hoa duoc anh ket qua."}), 500

        return send_file(
            io.BytesIO(encoded_image.tobytes()),
            mimetype="image/jpeg",
            as_attachment=False,
            download_name="ket_qua.jpg",
        )
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500
    finally:
        Path(temp_input_path).unlink(missing_ok=True)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
