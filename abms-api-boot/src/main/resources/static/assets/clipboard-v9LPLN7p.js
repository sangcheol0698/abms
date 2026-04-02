import{Y as c}from"./index-D2q0o4ur.js";/**
 * @license lucide-vue-next v0.544.0 - ISC
 *
 * This source code is licensed under the ISC license.
 * See the LICENSE file in the root directory of this source tree.
 */const i=c("trash",[["path",{d:"M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6",key:"miytrc"}],["path",{d:"M3 6h18",key:"d0wm0j"}],["path",{d:"M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2",key:"e791ji"}]]);async function r(t){if(navigator.clipboard?.writeText)try{await navigator.clipboard.writeText(t);return}catch{}const a=document.activeElement instanceof HTMLElement?document.activeElement:null,e=document.createElement("textarea");e.value=t,e.setAttribute("readonly",""),e.setAttribute("aria-hidden","true"),e.style.position="fixed",e.style.top="0",e.style.left="0",e.style.opacity="0",document.body.appendChild(e),e.focus(),e.select(),e.setSelectionRange(0,e.value.length);const o=document.execCommand("copy");if(document.body.removeChild(e),a?.focus(),!o)throw new Error("Failed to copy text")}export{i as T,r as c};
