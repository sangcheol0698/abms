@param String label
@param String value
@param String sub = null
@param String valueClass = ""
<div class="card px-x5 py-x4">
    <p class="stat-label">${label}</p>
    <p class="stat-value ${valueClass}">${value}</p>
    @if(sub != null)
        <p class="mt-x1 t3-regular text-fg-neutral-subtle">${sub}</p>
    @endif
</div>
