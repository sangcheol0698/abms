// ---------------------------------------------------------------------
// 지도·주소 입력
// - [data-map]: 카카오 지도에 마커를 그린다. 키(body[data-map-key])가 없거나 SDK 를 불러오지 못하면 서버가 그린 대체 내용을 그대로 둔다.
// - [data-address-field]: Daum 우편번호 서비스로 주소를 고르고, 카카오 Geocoder 로 좌표를 채운다.
// ---------------------------------------------------------------------
(function () {
    const mapKey = () => document.body.dataset.mapKey || '';
    const scripts = {};

    function loadScript(src) {
        if (!scripts[src]) {
            scripts[src] = new Promise((resolve, reject) => {
                const script = document.createElement('script');
                script.src = src;
                script.async = true;
                script.onload = resolve;
                script.onerror = () => {
                    delete scripts[src];
                    reject(new Error('스크립트를 불러오지 못했습니다: ' + src));
                };
                document.head.append(script);
            });
        }
        return scripts[src];
    }

    function kakaoReady() {
        if (!mapKey()) return Promise.reject(new Error('지도 키가 없습니다.'));
        return loadScript('https://dapi.kakao.com/v2/maps/sdk.js?autoload=false&libraries=services&appkey=' + encodeURIComponent(mapKey()))
            .then(() => new Promise((resolve) => kakao.maps.load(resolve)));
    }

    const postcodeReady = () => loadScript('https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js');

    function draw(el, markers, level) {
        const position = (m) => new kakao.maps.LatLng(m.lat, m.lng);
        el._fallback = el.innerHTML;
        el.innerHTML = '';
        const map = new kakao.maps.Map(el, {center: position(markers[0]), level: level || 3});
        map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
        const bounds = new kakao.maps.LatLngBounds();
        markers.forEach((m) => {
            new kakao.maps.Marker({map, position: position(m), title: m.label});
            const label = document.createElement(m.href ? 'a' : 'span');
            label.className = 'map-label';
            label.textContent = m.label;
            if (m.sub) label.title = m.sub;
            if (m.href) label.href = m.href;
            new kakao.maps.CustomOverlay({map, position: position(m), content: label, yAnchor: 1});
            bounds.extend(position(m));
        });
        if (markers.length > 1) map.setBounds(bounds, 40, 40, 40, 40);
        el._map = map;
        return map;
    }

    function renderMaps(root) {
        const targets = root.querySelectorAll('[data-map]:not([data-rendered])');
        if (!targets.length || !mapKey()) return;
        kakaoReady().then(() => {
            targets.forEach((el) => {
                if (el.dataset.rendered || !el.isConnected) return;
                const data = JSON.parse(el.dataset.map);
                if (!data.markers || !data.markers.length) return;
                el.dataset.rendered = 'true';
                draw(el, data.markers);
            });
        }).catch(() => {});
    }

    // 지도 마커로 포커스: [data-map-focus="위도,경도"] 를 누르면 같은 화면 지도의 중심을 옮긴다.
    document.addEventListener('click', (e) => {
        const trigger = e.target.closest('[data-map-focus]');
        if (!trigger) return;
        const map = document.querySelector('[data-map][data-rendered]');
        if (!map || !map._map) return;
        e.preventDefault();
        const [lat, lng] = trigger.dataset.mapFocus.split(',').map(Number);
        map._map.setLevel(3);
        map._map.panTo(new kakao.maps.LatLng(lat, lng));
        map.scrollIntoView({block: 'nearest', behavior: 'smooth'});
    });

    // ---- 주소 입력 ----
    function addressField(field) {
        const $ = (name) => field.querySelector('[data-address-' + name + ']');
        const zip = $('zip');
        const main = $('main');
        const detail = $('detail');
        const lat = $('lat');
        const lng = $('lng');
        const statusText = $('status-text');
        const preview = $('preview');

        const setStatus = (text) => statusText.textContent = text;

        function showPreview() {
            if (!preview || !lat.value || !lng.value) {
                if (preview) preview.classList.add('hidden');
                return;
            }
            kakaoReady().then(() => {
                preview.classList.remove('hidden');
                const marker = {label: main.value, lat: Number(lat.value), lng: Number(lng.value)};
                draw(preview, [marker], 4);
            }).catch(() => {});
        }

        function geocode() {
            lat.value = '';
            lng.value = '';
            const address = main.value.trim();
            if (!address) {
                setStatus('주소를 입력하지 않으면 위치 정보 없이 저장합니다.');
                showPreview();
                return;
            }
            if (!mapKey()) {
                setStatus('지도 키가 설정되지 않아 주소만 저장합니다.');
                return;
            }
            setStatus('지도 위치를 찾는 중…');
            kakaoReady().then(() => {
                new kakao.maps.services.Geocoder().addressSearch(address, (result, status) => {
                    if (status === kakao.maps.services.Status.OK && result.length) {
                        lat.value = Number(result[0].y).toFixed(7);
                        lng.value = Number(result[0].x).toFixed(7);
                        setStatus('지도 위치를 찾았습니다.');
                    } else {
                        setStatus('주소로 지도 위치를 찾지 못했습니다. 주소만 저장합니다.');
                    }
                    showPreview();
                });
            }).catch(() => setStatus('지도를 불러오지 못해 주소만 저장합니다.'));
        }

        // 팝업 차단을 피하려고 우편번호 스크립트는 미리 불러 두고, 클릭 시 바로 연다.
        postcodeReady().catch(() => {});
        $('search').addEventListener('click', () => {
            const open = () => new daum.Postcode({
                oncomplete(data) {
                    zip.value = data.zonecode || '';
                    main.value = data.roadAddress || data.jibunAddress || data.address;
                    geocode();
                    detail.focus();
                }
            }).open({q: main.value.trim() || undefined});
            if (typeof daum !== 'undefined' && daum.Postcode) {
                open();
            } else {
                postcodeReady().then(open).catch(() => setStatus('주소 검색을 불러오지 못했습니다. 주소를 직접 입력하세요.'));
            }
        });
        main.addEventListener('change', geocode);
        showPreview();
    }

    function enhance(root) {
        renderMaps(root);
        root.querySelectorAll('[data-address-field]:not([data-ready])').forEach((field) => {
            field.dataset.ready = 'true';
            addressField(field);
        });
    }

    // 히스토리 스냅샷에는 그려진 지도 대신 대체 내용을 남긴다. (뒤로 가기 후 다시 그린다)
    document.body.addEventListener('htmx:beforeHistorySave', () => {
        document.querySelectorAll('[data-map][data-rendered], [data-address-preview]').forEach((el) => {
            if (el._fallback !== undefined) el.innerHTML = el._fallback;
            delete el.dataset.rendered;
        });
        document.querySelectorAll('[data-address-field][data-ready]').forEach((el) => delete el.dataset.ready);
    });

    htmx.onLoad((el) => enhance(el));
})();
