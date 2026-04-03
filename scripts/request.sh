#!/usr/bin/env bash

curl -v \
    -H "Accept: application/json" \
    -H "Authorization: Bearer test" \
    -H "X-Forwarded-For: 127.0.0.1" \
    -H "X-Echo-Request: test" \
    http://localhost:8080/api/debug/headers