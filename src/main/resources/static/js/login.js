// 네비게이션 바 동적 로드
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById('header').innerHTML = `
        <nav class="navbar navbar-expand-lg navbar-light bg-light">
            <a class="navbar-brand" href="/">홈</a>
        </nav>
    `;
});

// 로그인 폼 제출 이벤트 처리
document.getElementById('login-form').addEventListener('submit', async function (event) {
    event.preventDefault(); // 폼 기본 제출 방지

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/user/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            window.location.href = '/'; // 로그인 성공 시 홈으로 리디렉션
        } else {
            const errorMessage = await response.text();
            showError(errorMessage); // 오류 메시지 표시
        }
    } catch (error) {
        showError('서버와의 통신 중 오류가 발생했습니다.');
    }
});

function showError(message) {
    const errorElement = document.getElementById('error-message');
    errorElement.style.display = 'block';
    errorElement.textContent = message;
}