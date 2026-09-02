(function () {
    document.querySelectorAll('.quiz').forEach(function (quiz) {
        var btn = quiz.querySelector('.reveal-btn');
        if (!btn) return;
        btn.addEventListener('click', function () {
            quiz.classList.toggle('revealed');
            btn.textContent = quiz.classList.contains('revealed') ? '收起答案' : '显示答案';
        });
    });
})();
