// K6 load test for Goodminton Shop API
// Run: k6 run -e API=http://localhost:8080 perf/mixed.js
//
// Ramp: 0 -> 20 users (30s) -> 50 users (2m) -> 0 (30s)
// Total: 3 phut, ~3000 requests
// Thresholds:
//   - p95 latency < 500ms
//   - error rate < 1%

import http from "k6/http";
import { check, sleep } from "k6";
import { Trend } from "k6/metrics";

const productList = new Trend("latency_product_list");
const productDetail = new Trend("latency_product_detail");
const productSearch = new Trend("latency_product_search");

export const options = {
  stages: [
    { duration: "30s", target: 20 },
    { duration: "2m", target: 50 },
    { duration: "30s", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<500", "p(99)<1500"],
    http_req_failed: ["rate<0.01"],
    latency_product_list: ["p(95)<300"],
    latency_product_detail: ["p(95)<200"],
    latency_product_search: ["p(95)<500"],
  },
};

const API = __ENV.API || "http://localhost:8080";
const PRODUCT_ID_MAX = parseInt(__ENV.PRODUCT_ID_MAX || "272");
const SEARCH_QUERIES = ["yonex", "astrox", "lining", "vot cau long", "giay"];

export default function () {
  // 1. Product list (pagination)
  let res = http.get(`${API}/api/products?page=1&size=20`);
  productList.add(res.timings.duration);
  check(res, { "list 200": (r) => r.status === 200 });

  sleep(0.5);

  // 2. Product detail (random id)
  const id = Math.ceil(Math.random() * PRODUCT_ID_MAX);
  res = http.get(`${API}/api/products/${id}`);
  productDetail.add(res.timings.duration);
  check(res, { "detail 200 or 404": (r) => r.status === 200 || r.status === 404 });

  sleep(0.5);

  // 3. Search (random query)
  const q = SEARCH_QUERIES[Math.floor(Math.random() * SEARCH_QUERIES.length)];
  res = http.get(`${API}/api/search/products?q=${encodeURIComponent(q)}&page=1&size=20`);
  productSearch.add(res.timings.duration);
  check(res, { "search 200": (r) => r.status === 200 });

  sleep(1);
}

export function handleSummary(data) {
  return {
    stdout: buildTextSummary(data),
    "perf/last-run.json": JSON.stringify(data, null, 2),
  };
}

function buildTextSummary(data) {
  const m = data.metrics;
  const lines = [
    "",
    "===== Goodminton Shop API - K6 Load Test =====",
    `Total requests: ${m.http_reqs.values.count}`,
    `Failed:         ${(m.http_req_failed.values.rate * 100).toFixed(2)}%`,
    `Throughput:     ${m.http_reqs.values.rate.toFixed(1)} req/s`,
    "",
    "Overall HTTP latency (ms):",
    `  p50:  ${m.http_req_duration.values["p(50)"].toFixed(0)}`,
    `  p95:  ${m.http_req_duration.values["p(95)"].toFixed(0)}`,
    `  p99:  ${m.http_req_duration.values["p(99)"].toFixed(0)}`,
    "",
    "Per-endpoint p95 (ms):",
    `  GET /api/products               ${m.latency_product_list?.values["p(95)"]?.toFixed(0) ?? "-"}`,
    `  GET /api/products/{id}          ${m.latency_product_detail?.values["p(95)"]?.toFixed(0) ?? "-"}`,
    `  GET /api/search/products?q=...  ${m.latency_product_search?.values["p(95)"]?.toFixed(0) ?? "-"}`,
    "",
    "============================================",
    "",
  ];
  return lines.join("\n");
}
