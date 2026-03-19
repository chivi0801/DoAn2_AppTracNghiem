from doctest import debug

from flask import Flask

app = Flask(__name__)

@app.route("/scan")
def scan():
    return "Kết nối thành công với Flask"

if __name__ == "__main__":
    app.run(host = "0.0.0.0", port = 5000) # ĐỪNG dùng host = 0.0.0.0 