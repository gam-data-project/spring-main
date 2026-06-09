(function () {
  const API = {
    report: "/renewal/settlementReport/report"
  };

  const el = {
    form: document.getElementById("reportSearchForm"),
    year: document.getElementById("reportYear"),
    tableBody: document.getElementById("reportTableBody"),
    caption: document.getElementById("reportCaption"),
    status: document.getElementById("reportStatus")
  };

  if (!el.form || !el.year || !el.tableBody) {
    return;
  }

  function initYearOptions() {
    const currentYear = new Date().getFullYear();
    const startYear = currentYear - 6;

    for (let year = currentYear; year >= startYear; year -= 1) {
      const option = document.createElement("option");
      option.value = String(year);
      option.textContent = String(year);
      el.year.appendChild(option);
    }

    el.year.value = String(currentYear);
  }

  function setStatus(text, state) {
    el.status.textContent = text;
    el.status.className = "report-status";

    if (state) {
      el.status.classList.add(`is-${state}`);
    }
  }

  function formatNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
  }

  function formatMonthLabel(ym) {
    if (!ym) {
      return "-";
    }

    if (ym === "총액") {
      return ym;
    }

    const match = /^(\d{4})-(\d{2})$/.exec(ym);
    if (!match) {
      return ym;
    }

    return `${Number(match[2])}월`;
  }

  function profitClass(value) {
    const amount = Number(value || 0);

    if (amount < 0) {
      return "profit-negative";
    }

    if (amount > 0) {
      return "profit-positive";
    }

    return "";
  }

  function renderEmpty(message) {
    el.tableBody.innerHTML = `
      <tr>
        <td colspan="5" class="report-empty">${message}</td>
      </tr>
    `;
  }

  function renderRows(items) {
    if (!Array.isArray(items) || items.length === 0) {
      renderEmpty("조회된 정산 데이터가 없습니다.");
      return;
    }

    el.tableBody.innerHTML = items.map((item, index) => {
      const isSummary = item.ym === "총액" || index === items.length - 1;
      const salesAmount = item.sales_amount ?? item.salesAmount;
      const purchaseAmount = item.purchase_amount ?? item.purchaseAmount;
      const expenseAmount = item.expense_amount ?? item.expenseAmount;
      const profitAmount = item.profit;
      const profitColorClass = profitClass(profitAmount);

      return `
        <tr class="${isSummary ? "summary-row" : ""}">
          <th scope="row">${formatMonthLabel(item.ym)}</th>
          <td>${formatNumber(salesAmount)}</td>
          <td>${formatNumber(purchaseAmount)}</td>
          <td>${formatNumber(expenseAmount)}</td>
          <td class="${profitColorClass}">${formatNumber(profitAmount)}</td>
        </tr>
      `;
    }).join("");
  }

  async function fetchReport(year) {
    setStatus("조회 중", "loading");
    el.caption.textContent = `${year}년 정산 데이터를 불러오는 중입니다.`;

    try {
      const query = new URLSearchParams({ year: String(year) });
      const response = await fetch(`${API.report}?${query.toString()}`);

      if (!response.ok) {
        throw new Error(`조회 실패 (${response.status})`);
      }

      const data = await response.json();
      renderRows(data.items || []);
      el.caption.textContent = `${data.year}년 월별 정산 현황입니다. 마지막 행은 총액입니다.`;
      setStatus("조회 완료", "success");
    } catch (error) {
      renderEmpty("정산 리포트를 불러오지 못했습니다.");
      el.caption.textContent = "잠시 후 다시 시도해 주세요.";
      setStatus("조회 실패", "error");
      console.error(error);
    }
  }

  el.form.addEventListener("submit", (event) => {
    event.preventDefault();
    fetchReport(el.year.value);
  });

  initYearOptions();
  fetchReport(el.year.value);
})();
