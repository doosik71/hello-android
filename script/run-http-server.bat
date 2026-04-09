pushd .
cd app\build\outputs\apk\debug
echo http://10.0.2.2:8888/
uv run python -m http.server 8888
popd