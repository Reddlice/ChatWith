# GPT-SoVITS API 接口文档

## 目录

- [1. API v1（传统接口）](#1-api-v1传统接口)
  - [1.1 启动服务](#11-启动服务)
  - [1.2 TTS 推理](#12-tts-推理)
  - [1.3 更换默认参考音频](#13-更换默认参考音频)
  - [1.4 切换模型权重](#14-切换模型权重)
  - [1.5 服务控制](#15-服务控制)
- [2. API v2（推荐接口）](#2-api-v2推荐接口)
  - [2.1 启动服务](#21-启动服务)
  - [2.2 TTS 推理](#22-tts-推理)
  - [2.3 切换模型权重](#23-切换模型权重)
  - [2.4 设置参考音频](#24-设置参考音频)
  - [2.5 服务控制](#25-服务控制)
- [3. 语音转文字（ASR）调用方案](#3-语音转文字asr调用方案)
  - [3.1 FunASR（中文/粤语）](#31-funasr中文粤语)
  - [3.2 Faster Whisper（多语种）](#32-faster-whisper多语种)
  - [3.3 封装 ASR API 服务](#33-封装-asr-api-服务)
- [4. 综合调用示例](#4-综合调用示例)

---

## 1. API v1（传统接口）

### 1.1 启动服务

```bash
python api.py -a 127.0.0.1 -p 9880
```

#### 启动参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `-s` | SoVITS 模型路径 | config.py 中指定 |
| `-g` | GPT 模型路径 | config.py 中指定 |
| `-dr` | 默认参考音频路径 | 空 |
| `-dt` | 默认参考音频文本 | 空 |
| `-dl` | 默认参考音频语种 | 空 |
| `-d` | 推理设备（cuda / cpu） | config.py 中指定 |
| `-a` | 绑定地址 | 0.0.0.0 |
| `-p` | 绑定端口 | 9880 |
| `-fp` | 使用全精度（覆盖 config） | false |
| `-hp` | 使用半精度（覆盖 config） | false |
| `-sm` | 流式返回模式（close / normal / keepalive） | close |
| `-mt` | 音频编码格式（wav / ogg / aac） | wav |
| `-st` | 音频数据类型（int16 / int32） | int16 |
| `-cp` | 文本切分符号（如 ",.，。"） | 空（不切分） |
| `-hb` | CNHubert 模型路径 | config.py 中指定 |
| `-b` | BERT 模型路径 | config.py 中指定 |

#### 语种映射表

| 用户输入 | 内部处理 |
|----------|----------|
| `zh` / `中文` | all_zh |
| `yue` / `粤语` | all_yue |
| `en` / `英文` | en |
| `ja` / `日文` | all_ja |
| `ko` / `韩文` | all_ko |
| `zh` / `中英混合` | zh |
| `yue` / `粤英混合` | yue |
| `ja` / `日英混合` | ja |
| `ko` / `韩英混合` | ko |
| `auto` / `多语种混合` | auto |
| `auto_yue` / `多语种混合(粤语)` | auto_yue |

---

### 1.2 TTS 推理

**Endpoint:** `GET /` 或 `POST /`

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `text` | str | 是 | - | 要合成的文本 |
| `text_language` | str | 是 | - | 文本语种（见语种映射表） |
| `refer_wav_path` | str | 否 | 启动参数 -dr | 参考音频路径 |
| `prompt_text` | str | 否 | 启动参数 -dt | 参考音频文本 |
| `prompt_language` | str | 否 | 启动参数 -dl | 参考音频语种 |
| `top_k` | int | 否 | 15 | Top-K 采样 |
| `top_p` | float | 否 | 1.0 | Top-P 采样 |
| `temperature` | float | 否 | 1.0 | 温度参数 |
| `speed` | float | 否 | 1.0 | 语速控制 |
| `cut_punc` | str | 否 | 启动参数 -cp | 文本切分符号 |
| `inp_refs` | list | 否 | [] | 辅助参考音频列表（多说话人融合） |
| `sample_steps` | int | 否 | 32 | VITS 采样步数（v3: 4/8/16/32/64/128, v4: 4/8/16/32） |
| `if_sr` | bool | 否 | false | 是否超分（仅 v3，24k→48k） |

#### 示例 1：使用启动时指定的默认参考音频

**GET 请求：**

```bash
curl "http://127.0.0.1:9880/?text=先帝创业未半而中道崩殂，今天下三分，益州疲弊，此诚危急存亡之秋也。&text_language=zh"
```

**POST 请求：**

```bash
curl -X POST "http://127.0.0.1:9880/" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "先帝创业未半而中道崩殂，今天下三分，益州疲弊，此诚危急存亡之秋也。",
    "text_language": "zh"
  }'
```

#### 示例 2：手动指定参考音频

**GET 请求：**

```bash
curl "http://127.0.0.1:9880/?refer_wav_path=reference.wav&prompt_text=今天天气真好。&prompt_language=zh&text=明天会更好吗？&text_language=zh"
```

**POST 请求：**

```bash
curl -X POST "http://127.0.0.1:9880/" \
  -H "Content-Type: application/json" \
  -d '{
    "refer_wav_path": "reference.wav",
    "prompt_text": "今天天气真好。",
    "prompt_language": "zh",
    "text": "明天会更好吗？",
    "text_language": "zh"
  }'
```

#### 示例 3：完整参数推理

**POST 请求：**

```bash
curl -X POST "http://127.0.0.1:9880/" \
  -H "Content-Type: application/json" \
  -d '{
    "refer_wav_path": "reference.wav",
    "prompt_text": "今天天气真好。",
    "prompt_language": "zh",
    "text": "明天会更好吗？",
    "text_language": "zh",
    "top_k": 20,
    "top_p": 0.6,
    "temperature": 0.6,
    "speed": 1.0,
    "cut_punc": "，。",
    "inp_refs": ["aux1.wav", "aux2.wav"],
    "sample_steps": 32,
    "if_sr": false
  }'
```

#### 响应

- **成功（200）：** 直接返回音频二进制流（Content-Type: audio/wav 或 audio/ogg 或 audio/aac）
- **失败（400）：** 返回 JSON

```json
{
  "code": 400,
  "message": "错误描述"
}
```

---

### 1.3 更换默认参考音频

**Endpoint:** `GET /change_refer` 或 `POST /change_refer`

在不重启服务的情况下更换默认参考音频。

```bash
curl "http://127.0.0.1:9880/change_refer?refer_wav_path=new_ref.wav&prompt_text=一二三。&prompt_language=zh"
```

**POST 请求：**

```bash
curl -X POST "http://127.0.0.1:9880/change_refer" \
  -H "Content-Type: application/json" \
  -d '{
    "refer_wav_path": "new_ref.wav",
    "prompt_text": "一二三。",
    "prompt_language": "zh"
  }'
```

**成功响应：**

```json
{ "code": 0, "message": "Success" }
```

---

### 1.4 切换模型权重

**Endpoint:** `GET /set_model` 或 `POST /set_model`

```bash
curl -X POST "http://127.0.0.1:9880/set_model" \
  -H "Content-Type: application/json" \
  -d '{
    "gpt_model_path": "GPT_SoVITS/pretrained_models/s1v3.ckpt",
    "sovits_model_path": "GPT_SoVITS/pretrained_models/v2Pro/s2Gv2Pro.pth"
  }'
```

---

### 1.5 服务控制

**Endpoint:** `GET /control` 或 `POST /control`

| command | 说明 |
|---------|------|
| `restart` | 重启服务进程 |
| `exit` | 停止服务进程 |

```bash
curl "http://127.0.0.1:9880/control?command=restart"
```

---

## 2. API v2（推荐接口）

API v2 基于 YAML 配置文件，支持流式传输、多版本切换、更灵活的参数控制，是推荐的调用方式。

### 2.1 启动服务

```bash
python api_v2.py -a 127.0.0.1 -p 9880 -c GPT_SoVITS/configs/tts_infer.yaml
```

#### 启动参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `-a` | 绑定地址 | 127.0.0.1 |
| `-p` | 绑定端口 | 9880 |
| `-c` | TTS 配置文件路径 | GPT_SoVITS/configs/tts_infer.yaml |

#### 配置文件示例（`tts_infer.yaml`）

```yaml
custom:
  bert_base_path: GPT_SoVITS/pretrained_models/chinese-roberta-wwm-ext-large
  cnhuhbert_base_path: GPT_SoVITS/pretrained_models/chinese-hubert-base
  device: cuda
  is_half: true
  t2s_weights_path: GPT_SoVITS/pretrained_models/gsv-v2final-pretrained/s1bert25hz-5kh-longer-epoch=12-step=369668.ckpt
  version: v2Pro
  vits_weights_path: GPT_SoVITS/pretrained_models/v2Pro/s2Gv2Pro.pth
```

支持的版本：`v1` / `v2` / `v2Pro` / `v2ProPlus` / `v3` / `v4`

---

### 2.2 TTS 推理

**Endpoint:** `GET /tts` 或 `POST /tts`

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `text` | str | **是** | - | 要合成的文本 |
| `text_lang` | str | **是** | - | 文本语种（zh/en/ja/ko/yue/auto 等） |
| `ref_audio_path` | str | **是** | - | 参考音频路径 |
| `prompt_lang` | str | **是** | - | 参考音频文本语种 |
| `prompt_text` | str | 否 | "" | 参考音频文本 |
| `aux_ref_audio_paths` | list | 否 | [] | 辅助参考音频路径（多说话人音色融合） |
| `top_k` | int | 否 | 15 | Top-K 采样 |
| `top_p` | float | 否 | 1.0 | Top-P 采样 |
| `temperature` | float | 否 | 1.0 | 温度参数 |
| `text_split_method` | str | 否 | "cut5" | 文本切分方法（见下方说明） |
| `batch_size` | int | 否 | 1 | 批处理大小 |
| `batch_threshold` | float | 否 | 0.75 | 批分割阈值 |
| `split_bucket` | bool | 否 | true | 是否分桶处理 |
| `speed_factor` | float | 否 | 1.0 | 语速因子 |
| `fragment_interval` | float | 否 | 0.3 | 音频片段间隔（秒） |
| `seed` | int | 否 | -1 | 随机种子（-1 为随机） |
| `media_type` | str | 否 | "wav" | 音频格式（wav/ogg/aac/raw） |
| `parallel_infer` | bool | 否 | true | 是否并行推理 |
| `repetition_penalty` | float | 否 | 1.35 | 重复惩罚系数 |
| `sample_steps` | int | 否 | 32 | VITS 采样步数 |
| `super_sampling` | bool | 否 | false | 是否超分（仅 v3 有效） |
| `streaming_mode` | bool/int | 否 | false | 流式模式（见下方说明） |
| `overlap_length` | int | 否 | 2 | 流式模式语义 token 重叠长度 |
| `min_chunk_length` | int | 否 | 16 | 流式模式最小 chunk 长度 |

#### 流式模式（streaming_mode）

| 值 | 说明 |
|----|------|
| `0` / `false` | 关闭流式，返回完整音频 |
| `1` / `true` | 最佳质量，返回分段片段（响应最慢） |
| `2` | 中等质量，真流式传输（响应较慢） |
| `3` | 低质量，固定长度 chunk 流式（响应最快） |

#### GET 请求示例

```bash
curl "http://127.0.0.1:9880/tts?text=你好世界&text_lang=zh&ref_audio_path=reference.wav&prompt_lang=zh&prompt_text=参考文本&media_type=wav&streaming_mode=false" -o output.wav
```

#### POST 请求示例

```bash
curl -X POST "http://127.0.0.1:9880/tts" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "你好，欢迎使用GPT-SoVITS语音合成系统。",
    "text_lang": "zh",
    "ref_audio_path": "reference.wav",
    "prompt_text": "这是一段参考音频的文本。",
    "prompt_lang": "zh",
    "top_k": 15,
    "top_p": 1.0,
    "temperature": 1.0,
    "text_split_method": "cut5",
    "batch_size": 1,
    "speed_factor": 1.0,
    "media_type": "wav",
    "streaming_mode": false,
    "parallel_infer": true,
    "repetition_penalty": 1.35,
    "sample_steps": 32,
    "super_sampling": false
  }' -o output.wav
```

#### Python 调用示例

```python
import requests

# 非流式
resp = requests.post(
    "http://127.0.0.1:9880/tts",
    json={
        "text": "你好，欢迎使用GPT-SoVITS语音合成系统。",
        "text_lang": "zh",
        "ref_audio_path": "reference.wav",
        "prompt_text": "这是一段参考音频的文本。",
        "prompt_lang": "zh",
        "media_type": "wav",
        "streaming_mode": False,
    },
)
if resp.status_code == 200:
    with open("output.wav", "wb") as f:
        f.write(resp.content)
else:
    print(resp.json())

# 流式
resp = requests.post(
    "http://127.0.0.1:9880/tts",
    json={
        "text": "流式语音合成测试。",
        "text_lang": "zh",
        "ref_audio_path": "reference.wav",
        "prompt_text": "参考文本。",
        "prompt_lang": "zh",
        "media_type": "ogg",
        "streaming_mode": 2,
    },
    stream=True,
)
with open("output.ogg", "wb") as f:
    for chunk in resp.iter_content(chunk_size=4096):
        if chunk:
            f.write(chunk)
```

#### 响应

- **成功（200）：** 直接返回音频二进制流
- **失败（400）：** 返回 JSON

```json
{
  "message": "错误描述"
}
```

---

### 2.3 切换模型权重

#### 切换 GPT 模型

**Endpoint:** `GET /set_gpt_weights`

```bash
curl "http://127.0.0.1:9880/set_gpt_weights?weights_path=GPT_SoVITS/pretrained_models/s1v3.ckpt"
```

#### 切换 SoVITS 模型

**Endpoint:** `GET /set_sovits_weights`

```bash
curl "http://127.0.0.1:9880/set_sovits_weights?weights_path=GPT_SoVITS/pretrained_models/v2Pro/s2Gv2Pro.pth"
```

---

### 2.4 设置参考音频

**Endpoint:** `GET /set_refer_audio`

```bash
curl "http://127.0.0.1:9880/set_refer_audio?refer_audio_path=reference.wav"
```

---

### 2.5 服务控制

**Endpoint:** `GET /control`

```bash
# 重启服务
curl "http://127.0.0.1:9880/control?command=restart"

# 停止服务
curl "http://127.0.0.1:9880/control?command=exit"
```

---

## 3. 语音转文字（ASR）调用方案

项目中提供了两套 ASR 引擎：

| 引擎 | 适用语种 | 特点 |
|------|----------|------|
| **FunASR**（达摩） | 中文、粤语 | 中文识别精度高，自带 VAD + 标点恢复 |
| **Faster Whisper** | 100+ 语种 | 多语种支持，中/粤语自动回退到 FunASR |

### 3.1 FunASR（中文/粤语）

**源码位置：** `tools/asr/funasr_asr.py`

#### 命令行调用

```bash
python tools/asr/funasr_asr.py \
  -i ./input_wavs/ \
  -o ./asr_output/ \
  -l zh
```

| 参数 | 说明 |
|------|------|
| `-i` | 输入文件夹路径（包含 wav 文件） |
| `-o` | 输出文件夹路径 |
| `-l` | 语种（zh / yue） |
| `-s` | 模型大小（默认 large） |

输出格式：每行 `音频路径|文件夹名|语种|识别文本`

#### Python 直接调用

```python
from tools.asr.funasr_asr import only_asr

text = only_asr(input_file="speech.wav", language="zh")
print(text)
```

#### 封装的 ASR API 服务端示例

```python
"""
funasr_api_server.py —— 基于 FunASR 的 ASR HTTP API 服务
启动: python funasr_api_server.py -p 9881
"""

import argparse
import sys
import os

sys.path.insert(0, os.getcwd())

import uvicorn
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from tools.asr.funasr_asr import create_model
import soundfile as sf
import tempfile

app = FastAPI()
model_cache = {}

@app.post("/asr")
async def asr(audio: UploadFile = File(...), language: str = Form("zh")):
    if language not in ("zh", "yue"):
        return JSONResponse({"code": 400, "message": f"unsupported language: {language}"}, status_code=400)

    try:
        if language not in model_cache:
            model_cache[language] = create_model(language)
        model = model_cache[language]
        contents = await audio.read()
        text = model.generate(input=contents)[0]["text"]
        return {"code": 0, "text": text}
    except Exception as e:
        return JSONResponse({"code": 500, "message": str(e)}, status_code=500)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-p", "--port", type=int, default=9881)
    parser.add_argument("-a", "--host", type=str, default="127.0.0.1")
    args = parser.parse_args()
    uvicorn.run(app, host=args.host, port=args.port, workers=1)
```

#### 客户端调用示例

```python
import requests

# 文件上传方式
with open("speech.wav", "rb") as f:
    resp = requests.post(
        "http://127.0.0.1:9881/asr",
        files={"audio": f},
        data={"language": "zh"},
    )
print(resp.json())  # {"code": 0, "text": "识别结果文本"}
```

```bash
# curl 调用
curl -X POST "http://127.0.0.1:9881/asr" \
  -F "audio=@speech.wav" \
  -F "language=zh"
```

---

### 3.2 Faster Whisper（多语种）

**源码位置：** `tools/asr/fasterwhisper_asr.py`

#### 命令行调用

```bash
python tools/asr/fasterwhisper_asr.py \
  -i ./input_wavs/ \
  -o ./asr_output/ \
  -s large-v3 \
  -l ja \
  -p float16
```

| 参数 | 说明 |
|------|------|
| `-i` | 输入文件夹路径 |
| `-o` | 输出文件夹路径 |
| `-s` | 模型大小（medium / large-v2 / large-v3 / large-v3-turbo） |
| `-l` | 语种（ja / en / ko / zh / yue / auto 等 100+ 语种） |
| `-p` | 推理精度（float16 / float32 / int8） |

> **注意：** 中/粤语会自动回退到 FunASR 以获得更好的识别效果。

#### 支持的语种代码（部分）

`en` `zh` `ja` `ko` `yue` `fr` `de` `es` `pt` `ru` `ar` `hi` `th` `vi` … 共 100+ 语种，完整列表见源码 `fasterwhisper_asr.py:language_code_list`。

#### 封装的 ASR API 服务端示例（多语种版）

```python
"""
whisper_api_server.py —— 基于 Faster Whisper 的多语种 ASR HTTP API 服务
启动: python whisper_api_server.py -p 9881
"""

import argparse
import sys
import os

sys.path.insert(0, os.getcwd())

import uvicorn
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from faster_whisper import WhisperModel
from tools.asr.funasr_asr import only_asr
import soundfile as sf
import tempfile
import torch
import io

app = FastAPI()
model_cache = {}

def load_whisper_model(model_size: str, precision: str):
    key = f"{model_size}_{precision}"
    if key not in model_cache:
        model_path = f"tools/asr/models/faster-whisper-{model_size}"
        device = "cuda" if torch.cuda.is_available() else "cpu"
        model_cache[key] = WhisperModel(model_path, device=device, compute_type=precision)
    return model_cache[key]


@app.post("/asr")
async def asr(
    audio: UploadFile = File(...),
    language: str = Form("auto"),
    model_size: str = Form("large-v3"),
    precision: str = Form("float16"),
):
    try:
        contents = await audio.read()
        model = load_whisper_model(model_size, precision)

        audio_path = tempfile.NamedTemporaryFile(suffix=".wav", delete=False).name
        with open(audio_path, "wb") as f:
            f.write(contents)
        segments, info = model.transcribe(
            audio=audio_path,
            beam_size=5,
            vad_filter=True,
            vad_parameters=dict(min_silence_duration_ms=700),
            language=None if language == "auto" else language,
        )
        os.unlink(audio_path)

        text = ""
        detected_lang = info.language
        if detected_lang in ["zh", "yue"]:
            text = only_asr(audio_path, language=detected_lang)
        else:
            text = "".join(seg.text for seg in segments)

        return {"code": 0, "text": text, "language": detected_lang}
    except Exception as e:
        return JSONResponse({"code": 500, "message": str(e)}, status_code=500)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-p", "--port", type=int, default=9881)
    parser.add_argument("-a", "--host", type=str, default="127.0.0.1")
    args = parser.parse_args()
    uvicorn.run(app, host=args.host, port=args.port, workers=1)
```

#### Python 直接调用 Faster Whisper

```python
from faster_whisper import WhisperModel
import torch

device = "cuda" if torch.cuda.is_available() else "cpu"
model = WhisperModel(
    "tools/asr/models/faster-whisper-large-v3",
    device=device,
    compute_type="float16",
)

segments, info = model.transcribe(
    "speech.wav",
    beam_size=5,
    vad_filter=True,
    vad_parameters=dict(min_silence_duration_ms=700),
    language=None,  # auto detect
)

text = "".join(seg.text for seg in segments)
print(f"语种: {info.language}, 文本: {text}")
```

---

## 4. 综合调用示例

下面是一个将 TTS 和 ASR 串联使用的 Python 示例：

```python
"""
TTS → ASR 闭环示例：
1. 将文本通过 TTS API 合成为语音
2. 将合成语音通过 ASR 转回文字（用于质量验证或数据集构建）
"""

import requests
import tempfile
import os

TTS_URL = "http://127.0.0.1:9880"
ASR_URL = "http://127.0.0.1:9881"


def tts(text: str, ref_audio: str, prompt_text: str, lang: str = "zh") -> bytes:
    """调用 TTS API 生成语音"""
    resp = requests.post(
        f"{TTS_URL}/tts",
        json={
            "text": text,
            "text_lang": lang,
            "ref_audio_path": ref_audio,
            "prompt_text": prompt_text,
            "prompt_lang": lang,
            "media_type": "wav",
            "streaming_mode": False,
        },
    )
    if resp.status_code != 200:
        raise Exception(f"TTS failed: {resp.json()}")
    return resp.content


def asr(audio_bytes: bytes, lang: str = "zh") -> str:
    """调用 ASR API 识别语音"""
    resp = requests.post(
        f"{ASR_URL}/asr",
        files={"audio": ("speech.wav", audio_bytes, "audio/wav")},
        data={"language": lang},
    )
    if resp.status_code != 200:
        raise Exception(f"ASR failed: {resp.json()}")
    return resp.json()["text"]


def tts_asr_loop(text: str, ref_audio: str, prompt_text: str):
    """TTS → ASR 闭环测试"""
    print(f"[原始文本] {text}")

    audio = tts(text, ref_audio, prompt_text)
    print(f"[TTS完成] 音频大小: {len(audio)} bytes")

    recognized = asr(audio)
    print(f"[ASR结果] {recognized}")

    return audio, recognized


if __name__ == "__main__":
    tts_asr_loop(
        text="你好，这是一个语音合成与识别闭环测试。",
        ref_audio="reference.wav",
        prompt_text="参考音频的对应文本。",
    )
```

---

## 错误码参考

| HTTP 状态码 | 含义 |
|-------------|------|
| 200 | 成功（TTS 返回音频流，控制类接口返回 JSON） |
| 400 | 请求参数错误（缺少必填参数、版本不支持、参数值非法等） |
| 500 | 服务端内部错误 |

## 注意事项

1. **路径必须是服务器本地路径** —— `ref_audio_path` 等文件路径是服务器可访问的路径，不支持通过 HTTP 上传文件。
2. **语种需要与配置版本兼容** —— 不同版本的 TTS 模型支持的语种范围不同，不支持的语种会返回 400 错误。
3. **流式模式** —— 建议非实时场景使用非流式模式（`streaming_mode: false`），实时场景使用 `streaming_mode: 2` 或 `3`。
4. **首次加载耗时** —— 模型加载和初始化需要一定时间，服务启动后需要等待模型加载完成。
5. **GPU 内存** —— 同时运行 TTS 和 ASR 服务时，请确保 GPU 显存充足，或分别部署在不同设备上。