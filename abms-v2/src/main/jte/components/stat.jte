@param String label
@param String value
@param String sub = null
@param String valueClass = ""
<div class="card px-5 py-4">
    <p class="stat-label">${label}</p>
    <p class="stat-value ${valueClass}">${value}</p>
    @if(sub != null)
        <p class="mt-1 text-xs text-slate-500">${sub}</p>
    @endif
</div>
