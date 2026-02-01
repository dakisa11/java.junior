
(function () {

    const exchangeRates = {
        EUR: 1.0,
        USD: 1.08,
        GBP: 0.85,
        CHF: 0.97,
        HRK: 7.53
    };

    function getSelectedCurrency() {
        const el = document.getElementById('currencySelect');
        return el ? el.value : 'EUR';
    }

    function setStoredCurrency(){
        const hiddenCurrency = document.getElementById("currency");
        if(hiddenCurrency)return hiddenCurrency.value = "EUR";
    }

    function calculateTotal() {
        let totalEUR = 0;

        document.querySelectorAll('.qty-input').forEach(input => {
            const qty = parseInt(input.value || '0', 10);
            if (qty <= 0) return;

            const row = input.closest('tr');
            const priceEl = row.querySelector('.item-price');
            if (!priceEl) return;

            const basePrice = parseFloat(priceEl.dataset.basePrice);
            if (!Number.isNaN(basePrice)) {
                totalEUR += qty * basePrice;
            }
        });

        return totalEUR;
    }

    function updateMenuPricesForDisplay(){
        const currency = getSelectedCurrency();
        const rate = exchangeRates[currency] || 1.0;

        document.querySelectorAll(".item-price").forEach(el =>{
            const base = parseFloat(el.dataset.basePrice);
            if(Number.isNaN(base))return;

            el.innerText = (base * rate).toFixed(2);
        })
    }

    function updateTotals(){
        const totalEUR = calculateTotal();

        const hiddenTotal = document.getElementById("totalPrice");
        if(hiddenTotal)hiddenTotal.value = totalEUR.toFixed(2);

        const currency = getSelectedCurrency();
        const rate = exchangeRates[currency] || 1.0;

        const displayTotal = document.getElementById("totalPriceDisplay");
        if(displayTotal)displayTotal.value = (totalEUR * rate).toFixed(2) + " " + currency;

        setStoredCurrency();
    }

    function callAll() {
        updateMenuPricesForDisplay();
        updateTotals();
    }

    document.addEventListener('DOMContentLoaded', () => {

        const currencySelect = document.getElementById('currencySelect');
        if (currencySelect) {
            currencySelect.addEventListener('change', callAll);
        }

        document.querySelectorAll('.qty-input')
            .forEach(i => i.addEventListener('input', updateTotals));

        callAll();
    });

})();
