(function () {
  const API = {
    searchPage: "/renewal/purchase/page",
    searchAllPage: "/renewal/purchase/page/all",
    save: "/renewal/purchase",
    update: (id) => `/renewal/purchase/${id}`,
    remove: (id) => `/renewal/purchase/${id}`,
    autoSearch: "/renewal/purchase/excel-search",
    autoSearchPage: "/renewal/purchase/excel-search"
  };

  const OPTION_API = {
    large: "/renewal/purchase/options/large",
    medium: "/renewal/purchase/options/medium",
    small: "/renewal/purchase/options/small",
    supplier: "/renewal/purchase/options/supplier",
    products: "/renewal/purchase/options/products"
  };

  const DEFAULTS = {
    all: "전체",
    save: "선택"
  };

  const PAGE_WINDOW_SIZE = 10;

  const state = {
    selectedId: null,
    selectedRow: null,
    mode: "search",
    currentPage: 1,
    size: 10,
    totalPages: 0,
    totalCount: 0,
    rows: [],
    autoCurrentPage: 1,
    autoSize: 10,
    autoTotalPages: 0,
    autoTotalCount: 0,
    autoRows: []
  };

  const el = {
    searchLarge: document.getElementById("searchLargeCategory"),
    searchMedium: document.getElementById("searchMediumCategory"),
    searchSmall: document.getElementById("searchSmallCategory"),
    searchType: document.getElementById("searchPurchaseType"),
    searchStartDate: document.getElementById("searchStartDate"),
    searchEndDate: document.getElementById("searchEndDate"),
    btnSearch: document.getElementById("btnSearch"),
    btnSearchAll: document.getElementById("btnSearchAll"),
    tableBody: document.getElementById("purchaseTableBody"),
    purchaseId: document.getElementById("purchaseId"),
    saveLarge: document.getElementById("saveLargeCategory"),
    saveMedium: document.getElementById("saveMediumCategory"),
    saveSmall: document.getElementById("saveSmallCategory"),
    saveType: document.getElementById("savePurchaseType"),
    saveDate: document.getElementById("savePurchaseDate"),
    saveTime: document.getElementById("savePurchaseTime"),
    saveSupplier: document.getElementById("saveSupplierName"),
    saveUnitCost: document.getElementById("saveUnitCost"),
    saveQty: document.getElementById("saveQuantity"),
    saveTotal: document.getElementById("saveTotalCost"),
    btnSave: document.getElementById("btnSave"),
    btnUpdate: document.getElementById("btnUpdate"),
    btnDelete: document.getElementById("btnDelete"),
    autoStartDate: document.getElementById("autoStartDate"),
    autoEndDate: document.getElementById("autoEndDate"),
    autoSupplier: document.getElementById("autoSupplierName"),
    btnAutoSearch: document.getElementById("btnAutoSearch"),
    autoUploadFileName: document.getElementById("autoUploadFileName"),
    autoUploadFile: document.getElementById("autoUploadFile"),
    btnAutoUpload: document.getElementById("btnAutoUpload"),
    autoResultBody: document.getElementById("autoResultBody"),
    autoPagination: document.getElementById("autoPagination"),
    autoPageInfo: document.getElementById("autoPageInfo"),
    pagination: document.getElementById("purchasePagination"),
    pageInfo: document.getElementById("purchasePageInfo")
  };

  const normalize = (value) => String(value ?? "").trim();
  const toNumber = (value) => Number(value || 0);
  const won = (value) => toNumber(value).toLocaleString();

  function recalcTotal() {
    el.saveTotal.value = toNumber(el.saveUnitCost.value) * toNumber(el.saveQty.value);
  }

  function buildQuery(params) {
    const q = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && normalize(value) !== "") {
        q.append(key, value);
      }
    });
    return q.toString();
  }

  async function requestJson(url, options = {}) {
    const hasBody = Boolean(options.body);
    const useJsonHeader = hasBody && !(options.body instanceof FormData);

    const res = await fetch(url, {
      headers: useJsonHeader ? { "Content-Type": "application/json" } : undefined,
      ...options
    });

    if (!res.ok) throw new Error(`요청 실패 (${res.status})`);
    if (res.status === 204) return null;

    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) return res.json();
    return null;
  }

  function extractOptionValue(item) {
    if (item == null) return "";
    if (typeof item === "string") return normalize(item);
    if (typeof item === "object") return normalize(item.value ?? item.name ?? "");
    return "";
  }

  function resetSelect(selectEl, placeholder) {
    selectEl.innerHTML = "";
    const option = document.createElement("option");
    option.value = placeholder;
    option.textContent = placeholder;
    selectEl.appendChild(option);
    selectEl.value = placeholder;
  }

  function setSelectOptions(selectEl, rows, placeholder) {
    resetSelect(selectEl, placeholder);
    const values = Array.from(new Set((rows || []).map(extractOptionValue).filter(Boolean)));
    values.forEach((value) => {
      const option = document.createElement("option");
      option.value = value;
      option.textContent = value;
      selectEl.appendChild(option);
    });
  }

  function setSelectValueIfExists(selectEl, value, fallback) {
    const v = normalize(value);
    if (!v) {
      selectEl.value = fallback;
      return;
    }
    const exists = Array.from(selectEl.options).some((opt) => opt.value === v);
    selectEl.value = exists ? v : fallback;
  }

  function toTimeInputValue(value) {
    const raw = normalize(value);
    if (!raw) return "";
    if (/^\d{2}:\d{2}:\d{2}$/.test(raw)) return raw;
    return raw.length >= 5 ? raw.slice(0, 5) : raw;
  }

  function isSavePlaceholder(value) {
    const v = normalize(value);
    return !v || v === DEFAULTS.save;
  }

  function ensureCommandSuccess(response, fallbackMessage) {
    if (!response) throw new Error(fallbackMessage);
    if (response.success === false) throw new Error(response.message || fallbackMessage);
    return response;
  }

  async function loadSearchLargeOptions() {
    const rows = await requestJson(OPTION_API.large);
    setSelectOptions(el.searchLarge, rows, DEFAULTS.all);
    el.searchLarge.value = DEFAULTS.all;
  }

  async function loadSearchMediumOptions() {
    const largeCategory = normalize(el.searchLarge.value) || DEFAULTS.all;

    resetSelect(el.searchMedium, DEFAULTS.all);
    resetSelect(el.searchSmall, DEFAULTS.all);
    resetSelect(el.searchType, DEFAULTS.all);

    if (largeCategory === DEFAULTS.all) return;

    const query = buildQuery({ largeCategory });
    const rows = await requestJson(`${OPTION_API.medium}?${query}`);
    setSelectOptions(el.searchMedium, rows, DEFAULTS.all);
    el.searchMedium.value = DEFAULTS.all;
  }

  async function loadSearchSmallOptions() {
    const largeCategory = normalize(el.searchLarge.value) || DEFAULTS.all;
    const mediumCategory = normalize(el.searchMedium.value) || DEFAULTS.all;

    resetSelect(el.searchSmall, DEFAULTS.all);
    resetSelect(el.searchType, DEFAULTS.all);

    if (largeCategory === DEFAULTS.all || mediumCategory === DEFAULTS.all) return;

    const query = buildQuery({ largeCategory, mediumCategory });
    const rows = await requestJson(`${OPTION_API.small}?${query}`);
    setSelectOptions(el.searchSmall, rows, DEFAULTS.all);
    el.searchSmall.value = DEFAULTS.all;
  }

  async function loadSearchSupplierOptions() {
    const largeCategory = normalize(el.searchLarge.value) || DEFAULTS.all;
    const mediumCategory = normalize(el.searchMedium.value) || DEFAULTS.all;
    const smallCategory = normalize(el.searchSmall.value) || DEFAULTS.all;

    resetSelect(el.searchType, DEFAULTS.all);

    if (
      largeCategory === DEFAULTS.all ||
      mediumCategory === DEFAULTS.all ||
      smallCategory === DEFAULTS.all
    ) {
      return;
    }

    const query = buildQuery({ largeCategory, mediumCategory, smallCategory });
    const rows = await requestJson(`${OPTION_API.supplier}?${query}`);
    setSelectOptions(el.searchType, rows, DEFAULTS.all);
    el.searchType.value = DEFAULTS.all;
  }

  async function loadSaveLargeOptions() {
    const rows = await requestJson(OPTION_API.large);
    setSelectOptions(el.saveLarge, rows, DEFAULTS.save);
    el.saveLarge.value = DEFAULTS.save;
  }

  async function loadSaveMediumOptions() {
    const largeCategory = normalize(el.saveLarge.value);

    resetSelect(el.saveMedium, DEFAULTS.save);
    resetSelect(el.saveSmall, DEFAULTS.save);
    resetSelect(el.saveType, DEFAULTS.save);

    if (isSavePlaceholder(largeCategory)) return;

    const query = buildQuery({ largeCategory });
    const rows = await requestJson(`${OPTION_API.medium}?${query}`);
    setSelectOptions(el.saveMedium, rows, DEFAULTS.save);
    el.saveMedium.value = DEFAULTS.save;
  }

  async function loadSaveSmallOptions() {
    const largeCategory = normalize(el.saveLarge.value);
    const mediumCategory = normalize(el.saveMedium.value);

    resetSelect(el.saveSmall, DEFAULTS.save);
    resetSelect(el.saveType, DEFAULTS.save);

    if (isSavePlaceholder(largeCategory) || isSavePlaceholder(mediumCategory)) return;

    const query = buildQuery({ largeCategory, mediumCategory });
    const rows = await requestJson(`${OPTION_API.small}?${query}`);
    setSelectOptions(el.saveSmall, rows, DEFAULTS.save);
    el.saveSmall.value = DEFAULTS.save;
  }

  async function loadSaveProductOptions() {
    const largeCategory = normalize(el.saveLarge.value);
    const mediumCategory = normalize(el.saveMedium.value);
    const smallCategory = normalize(el.saveSmall.value);

    resetSelect(el.saveType, DEFAULTS.save);

    if (
      isSavePlaceholder(largeCategory) ||
      isSavePlaceholder(mediumCategory) ||
      isSavePlaceholder(smallCategory)
    ) {
      return;
    }

    const query = buildQuery({ largeCategory, mediumCategory, smallCategory });
    const rows = await requestJson(`${OPTION_API.products}?${query}`);
    setSelectOptions(el.saveType, rows, DEFAULTS.save);
    el.saveType.value = DEFAULTS.save;
  }

  async function applyCategoryToSaveForm(row) {
    const largeCategory = normalize(row.largeCategory ?? row.large_category);
    const mediumCategory = normalize(row.mediumCategory ?? row.medium_category);
    const smallCategory = normalize(row.smallCategory ?? row.small_category);
    const productName = normalize(row.productName ?? row.product_name);

    await loadSaveLargeOptions();
    setSelectValueIfExists(el.saveLarge, largeCategory, DEFAULTS.save);

    await loadSaveMediumOptions();
    setSelectValueIfExists(el.saveMedium, mediumCategory, DEFAULTS.save);

    await loadSaveSmallOptions();
    setSelectValueIfExists(el.saveSmall, smallCategory, DEFAULTS.save);

    await loadSaveProductOptions();
    setSelectValueIfExists(el.saveType, productName, DEFAULTS.save);
  }

  async function fillForm(row) {
    state.selectedId = row.id ?? null;
    state.selectedRow = row ?? null;
    el.purchaseId.value = row.id ?? "";

    await applyCategoryToSaveForm(row);
    el.saveDate.value = normalize(row.purchaseDate ?? row.purchase_date);
    el.saveTime.value = toTimeInputValue(row.purchaseTime ?? row.purchase_time);
    el.saveSupplier.value = normalize(row.supplierName ?? row.supplier_name ?? row.description);
    el.saveUnitCost.value = toNumber(row.unitCost ?? row.unit_cost);
    el.saveQty.value = toNumber(row.quantity);
    el.saveTotal.value = toNumber(row.totalCost ?? row.total_cost);
  }

  function renderRows(rows) {
    el.tableBody.innerHTML = "";
    if (!rows.length) {
      el.tableBody.innerHTML = '<tr><td colspan="8">조회 결과가 없습니다.</td></tr>';
      return;
    }

    rows.forEach((row) => {
      const tr = document.createElement("tr");
      tr.style.cursor = "pointer";
      tr.innerHTML = `
        <td>${row.largeCategory ?? row.large_category ?? "-"}</td>
        <td>${row.mediumCategory ?? row.medium_category ?? "-"}</td>
        <td>${row.smallCategory ?? row.small_category ?? "-"}</td>
        <td>${row.supplierName ?? row.supplier_name ?? "-"}</td>
        <td>${row.productName ?? row.product_name ?? "-"}</td>
        <td class="text-end">${won(row.unitCost ?? row.unit_cost)}</td>
        <td class="text-end">${won(row.quantity)}</td>
        <td class="text-end">${won(row.totalCost ?? row.total_cost)}</td>
      `;
      tr.addEventListener("click", async () => {
        try {
          await fillForm(row);
        } catch (error) {
          alert(error.message || "선택 항목 반영 중 오류가 발생했습니다.");
        }
      });
      el.tableBody.appendChild(tr);
    });
  }

  function getPayload() {
    const largeCategory = normalize(el.saveLarge.value);
    const mediumCategory = normalize(el.saveMedium.value);
    const smallCategory = normalize(el.saveSmall.value);
    const productName = normalize(el.saveType.value);

    let purchaseTime = normalize(el.saveTime.value) || null;
    const selectedRawTime = normalize(state.selectedRow?.purchaseTime ?? state.selectedRow?.purchase_time);
    if (
      purchaseTime &&
      /^\d{2}:\d{2}$/.test(purchaseTime) &&
      /^\d{2}:\d{2}:\d{2}$/.test(selectedRawTime) &&
      selectedRawTime.startsWith(`${purchaseTime}:`)
    ) {
      // 브라우저가 초 단위를 표시/입력하지 못하는 경우, 목록에서 고른 원본 시간을 유지한다.
      purchaseTime = selectedRawTime;
    }

    return {
      large_category: isSavePlaceholder(largeCategory) ? null : largeCategory,
      medium_category: isSavePlaceholder(mediumCategory) ? null : mediumCategory,
      small_category: isSavePlaceholder(smallCategory) ? null : smallCategory,
      product_name: isSavePlaceholder(productName) ? null : productName,
      purchase_date: normalize(el.saveDate.value),
      purchase_time: purchaseTime,
      supplier_name: normalize(el.saveSupplier.value),
      unit_cost: toNumber(el.saveUnitCost.value),
      quantity: toNumber(el.saveQty.value),
      total_cost: toNumber(el.saveTotal.value)
    };
  }

  function getSearchParams() {
    return {
      largeCategory: normalize(el.searchLarge.value) || DEFAULTS.all,
      mediumCategory: normalize(el.searchMedium.value) || DEFAULTS.all,
      smallCategory: normalize(el.searchSmall.value) || DEFAULTS.all,
      supplierName: normalize(el.searchType.value) || DEFAULTS.all,
      startDate: normalize(el.searchStartDate.value),
      endDate: normalize(el.searchEndDate.value)
    };
  }

  function renderPageInfo() {
    if (!el.pageInfo) return;
    if (state.totalCount === 0) {
      el.pageInfo.textContent = "총 0건";
      return;
    }
    el.pageInfo.textContent = `총 ${state.totalCount}건 · ${state.currentPage}/${state.totalPages} 페이지`;
  }

  function createPageItem({ label, page, disabled = false, active = false }) {
    const li = document.createElement("li");
    li.className = `page-item${disabled ? " disabled" : ""}${active ? " active" : ""}`;

    const button = document.createElement("button");
    button.type = "button";
    button.className = "page-link";
    button.textContent = label;

    if (!disabled && !active) {
      button.addEventListener("click", async () => {
        try {
          if (state.mode === "all") {
            await fetchAllPage(page);
          } else {
            await fetchSearchPage(page);
          }
        } catch (error) {
          alert(error.message || "페이지 조회 중 오류가 발생했습니다.");
        }
      });
    }

    li.appendChild(button);
    return li;
  }

  function renderPagination() {
    if (!el.pagination) return;
    el.pagination.innerHTML = "";
    if (!state.totalPages || state.totalPages < 1) return;

    const currentBlock = Math.floor((state.currentPage - 1) / PAGE_WINDOW_SIZE);
    const startPage = currentBlock * PAGE_WINDOW_SIZE + 1;
    const endPage = Math.min(startPage + PAGE_WINDOW_SIZE - 1, state.totalPages);

    el.pagination.appendChild(
      createPageItem({
        label: "<",
        page: Math.max(1, startPage - 1),
        disabled: startPage <= 1
      })
    );

    for (let page = startPage; page <= endPage; page += 1) {
      el.pagination.appendChild(
        createPageItem({
          label: String(page),
          page,
          active: page === state.currentPage
        })
      );
    }

    el.pagination.appendChild(
      createPageItem({
        label: ">",
        page: Math.min(state.totalPages, endPage + 1),
        disabled: endPage >= state.totalPages
      })
    );
  }

  function renderPagedResult(response, fallbackPage) {
    const items = Array.isArray(response?.items) ? response.items : [];
    const totalCount = Number(response?.totalCount ?? response?.total_count ?? 0);
    const totalPages = Number(response?.totalPages ?? response?.total_pages ?? 0);
    const currentPage = Number(response?.page ?? fallbackPage);

    state.rows = items;
    state.totalCount = Number.isFinite(totalCount) ? totalCount : 0;
    state.totalPages = Number.isFinite(totalPages) ? totalPages : 0;
    state.currentPage = Number.isFinite(currentPage) ? currentPage : fallbackPage;

    renderRows(state.rows);
    renderPageInfo();
    renderPagination();
  }

  async function fetchSearchPage(page = 1) {
    const filters = getSearchParams();
    const query = buildQuery({
      ...filters,
      page,
      size: state.size
    });

    const response = await requestJson(`${API.searchPage}?${query}`);
    renderPagedResult(response, page);
  }

  async function fetchAllPage(page = 1) {
    const query = buildQuery({ page, size: state.size });
    const url = query ? `${API.searchAllPage}?${query}` : API.searchAllPage;
    const response = await requestJson(url);
    renderPagedResult(response, page);
  }

  async function search() {
    state.mode = "search";
    await fetchSearchPage(1);
  }

  async function searchAll() {
    state.mode = "all";
    el.searchStartDate.value = "";
    el.searchEndDate.value = "";
    el.searchLarge.value = DEFAULTS.all;
    await loadSearchMediumOptions();
    await fetchAllPage(1);
  }

  async function refreshCurrentPage() {
    if (state.mode === "all") {
      await fetchAllPage(state.currentPage || 1);
      return;
    }
    await fetchSearchPage(state.currentPage || 1);
  }

  async function save() {
    try {
      const response = await requestJson(API.save, {
        method: "POST",
        body: JSON.stringify(getPayload())
      });
      const result = ensureCommandSuccess(response, "저장에 실패했습니다.");
      await refreshCurrentPage();
      alert(result.message || "저장 완료");
    } catch (error) {
      alert(error.message);
    }
  }

  async function update() {
    try {
      if (!state.selectedId) {
        alert("수정할 항목을 목록에서 먼저 선택해 주세요.");
        return;
      }

      const response = await requestJson(API.update(state.selectedId), {
        method: "PUT",
        body: JSON.stringify(getPayload())
      });
      const result = ensureCommandSuccess(response, "수정에 실패했습니다.");
      await refreshCurrentPage();
      alert(result.message || "수정 완료");
    } catch (error) {
      alert(error.message);
    }
  }

  async function removePurchase() {
    try {
      if (!state.selectedId) {
        alert("삭제할 항목을 목록에서 먼저 선택해 주세요.");
        return;
      }

      const response = await requestJson(API.remove(state.selectedId), { method: "DELETE" });
      const result = ensureCommandSuccess(response, "삭제에 실패했습니다.");
      state.selectedId = null;
      state.selectedRow = null;
      el.purchaseId.value = "";
      await refreshCurrentPage();
      alert(result.message || "삭제 완료");
    } catch (error) {
      alert(error.message);
    }
  }

  async function autoSearchLegacy() {
    try {
      if (!el.autoUploadFile.files.length) {
        alert("계좌내역 파일을 먼저 선택해 주세요.");
        return;
      }

      const formData = new FormData();
      formData.append("file", el.autoUploadFile.files[0]);
      formData.append("startDate", el.autoStartDate.value);
      formData.append("endDate", el.autoEndDate.value);
      formData.append("supplierName", el.autoSupplier.value.trim());

      const res = await fetch(API.autoSearch, { method: "POST", body: formData });
      if (!res.ok) throw new Error(`조회 실패 (${res.status})`);

      const data = await res.json();
      const rows = Array.isArray(data) ? data : (data.items ?? []);
      el.autoResultBody.innerHTML = "";

      if (!rows.length) {
        el.autoResultBody.innerHTML = '<tr><td colspan="3">조회 결과가 없습니다.</td></tr>';
        return;
      }

      rows.forEach((row) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${row.purchaseDate ?? row.purchase_date ?? row.date ?? "-"}</td>
          <td>${row.supplierName ?? row.supplier_name ?? row.vendorName ?? "-"}</td>
          <td class="text-end">${won(row.totalCost ?? row.total_cost ?? row.amount ?? 0)}</td>
        `;
        el.autoResultBody.appendChild(tr);
      });
    } catch (error) {
      alert(error.message);
    }
  }

  function renderAutoRowsPaged(rows) {
    if (!el.autoResultBody) return;
    el.autoResultBody.innerHTML = "";

    if (!rows.length) {
      el.autoResultBody.innerHTML = '<tr><td colspan="3">조회 결과가 없습니다.</td></tr>';
      return;
    }

    rows.forEach((row) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${row.purchaseDate ?? row.purchase_date ?? row.date ?? "-"}</td>
        <td>${row.supplierName ?? row.supplier_name ?? row.vendorName ?? "-"}</td>
        <td class="text-end">${won(row.totalCost ?? row.total_cost ?? row.amount ?? 0)}</td>
      `;
      el.autoResultBody.appendChild(tr);
    });
  }

  function renderAutoPageInfo() {
    if (!el.autoPageInfo) return;
    if (state.autoTotalCount === 0) {
      el.autoPageInfo.textContent = "총 0건";
      return;
    }
    el.autoPageInfo.textContent = `총 ${state.autoTotalCount}건 · ${state.autoCurrentPage}/${state.autoTotalPages} 페이지`;
  }

  function createAutoPageItem({ label, page, disabled = false, active = false }) {
    const li = document.createElement("li");
    li.className = `page-item${disabled ? " disabled" : ""}${active ? " active" : ""}`;

    const button = document.createElement("button");
    button.type = "button";
    button.className = "page-link";
    button.textContent = label;

    if (!disabled && !active) {
      button.addEventListener("click", async () => {
        try {
          await fetchAutoPage(page);
        } catch (error) {
          alert(error.message || "페이지 조회 중 오류가 발생했습니다.");
        }
      });
    }

    li.appendChild(button);
    return li;
  }

  function renderAutoPagination() {
    if (!el.autoPagination) return;
    el.autoPagination.innerHTML = "";
    if (!state.autoTotalPages || state.autoTotalPages < 1) return;

    const currentBlock = Math.floor((state.autoCurrentPage - 1) / PAGE_WINDOW_SIZE);
    const startPage = currentBlock * PAGE_WINDOW_SIZE + 1;
    const endPage = Math.min(startPage + PAGE_WINDOW_SIZE - 1, state.autoTotalPages);

    el.autoPagination.appendChild(
      createAutoPageItem({
        label: "<",
        page: Math.max(1, startPage - 1),
        disabled: startPage <= 1
      })
    );

    for (let page = startPage; page <= endPage; page += 1) {
      el.autoPagination.appendChild(
        createAutoPageItem({
          label: String(page),
          page,
          active: page === state.autoCurrentPage
        })
      );
    }

    el.autoPagination.appendChild(
      createAutoPageItem({
        label: ">",
        page: Math.min(state.autoTotalPages, endPage + 1),
        disabled: endPage >= state.autoTotalPages
      })
    );
  }

  function renderAutoPagedResult(response, fallbackPage) {
    let items = [];
    let totalCount = 0;
    let totalPages = 0;
    let currentPage = fallbackPage;

    if (Array.isArray(response)) {
      totalCount = response.length;
      totalPages = Math.ceil(totalCount / state.autoSize);
      currentPage = Math.max(1, Math.min(fallbackPage, Math.max(1, totalPages)));
      const start = (currentPage - 1) * state.autoSize;
      const end = start + state.autoSize;
      items = response.slice(start, end);
    } else {
      items = Array.isArray(response?.items) ? response.items : [];
      totalCount = Number(response?.totalCount ?? response?.total_count ?? items.length);
      totalPages = Number(response?.totalPages ?? response?.total_pages ?? Math.ceil(totalCount / state.autoSize));
      currentPage = Number(response?.page ?? fallbackPage);
    }

    state.autoRows = items;
    state.autoTotalCount = Number.isFinite(totalCount) ? totalCount : 0;
    state.autoTotalPages = Number.isFinite(totalPages) ? totalPages : 0;
    state.autoCurrentPage = Number.isFinite(currentPage) ? currentPage : fallbackPage;

    renderAutoRowsPaged(state.autoRows);
    renderAutoPageInfo();
    renderAutoPagination();
  }

  async function fetchAutoPage(page = 1) {
    if (!el.autoUploadFile.files.length) {
      alert("계좌내역 파일을 먼저 선택해 주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("file", el.autoUploadFile.files[0]);
    formData.append("startDate", el.autoStartDate.value);
    formData.append("endDate", el.autoEndDate.value);
    formData.append("supplierName", el.autoSupplier.value.trim());
    formData.append("page", String(page));
    formData.append("size", String(state.autoSize));

    let response;
    try {
      response = await requestJson(API.autoSearchPage, { method: "POST", body: formData });
    } catch (error) {
      if (String(error.message || "").includes("(404)")) {
        response = await requestJson(API.autoSearch, { method: "POST", body: formData });
      } else {
        throw error;
      }
    }

    renderAutoPagedResult(response, page);
  }

  async function autoSearch() {
    try {
      await fetchAutoPage(1);
    } catch (error) {
      alert(error.message);
    }
  }

  el.saveUnitCost.addEventListener("input", recalcTotal);
  el.saveQty.addEventListener("input", recalcTotal);
  el.btnSearch.addEventListener("click", search);
  el.btnSearchAll.addEventListener("click", searchAll);
  el.btnSave.addEventListener("click", save);
  el.btnUpdate.addEventListener("click", update);
  el.btnDelete.addEventListener("click", removePurchase);
  el.btnAutoSearch.addEventListener("click", autoSearch);
  el.autoUploadFileName.addEventListener("click", () => el.autoUploadFile.click());
  el.autoUploadFile.addEventListener("change", () => {
    const file = el.autoUploadFile.files[0];
    el.autoUploadFileName.value = file ? file.name : "";
  });
  el.btnAutoUpload.addEventListener("click", () => el.autoUploadFile.click());

  el.searchLarge.addEventListener("change", async () => {
    try {
      await loadSearchMediumOptions();
    } catch (error) {
      alert(error.message);
    }
  });

  el.searchMedium.addEventListener("change", async () => {
    try {
      await loadSearchSmallOptions();
    } catch (error) {
      alert(error.message);
    }
  });

  el.searchSmall.addEventListener("change", async () => {
    try {
      await loadSearchSupplierOptions();
    } catch (error) {
      alert(error.message);
    }
  });

  el.saveLarge.addEventListener("change", async () => {
    try {
      state.selectedId = null;
      state.selectedRow = null;
      el.purchaseId.value = "";
      await loadSaveMediumOptions();
    } catch (error) {
      alert(error.message);
    }
  });

  el.saveMedium.addEventListener("change", async () => {
    try {
      state.selectedId = null;
      state.selectedRow = null;
      el.purchaseId.value = "";
      await loadSaveSmallOptions();
    } catch (error) {
      alert(error.message);
    }
  });

  el.saveSmall.addEventListener("change", async () => {
    try {
      state.selectedId = null;
      state.selectedRow = null;
      el.purchaseId.value = "";
      await loadSaveProductOptions();
    } catch (error) {
      alert(error.message);
    }
  });

  (async function initPurchasePage() {
    try {
      if (el.saveTime) {
        el.saveTime.step = "1";
      }
      resetSelect(el.saveType, DEFAULTS.save);
      await loadSearchLargeOptions();
      await loadSearchMediumOptions();
      await loadSaveLargeOptions();
      await loadSaveMediumOptions();
      await search();
    } catch (error) {
      alert(error.message);
    }
  })();
})();
