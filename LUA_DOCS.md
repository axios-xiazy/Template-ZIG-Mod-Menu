# <img src="https://upload.wikimedia.org/wikipedia/commons/c/cf/Lua-Logo.svg" width="30" height="30"> Palladium Lua Engine

Welcome to the **Palladium Lua Engine**, a powerful scripting environment embedded directly into your mod menu. No need to recompile the APK—just write, run, and hack dynamically!

---

## 🛠️ API Reference

All commands are accessed via the `palladium` namespace.

### 1. 🔍 Library & Base Address
Stop guessing hardcoded addresses. Find them dynamically.

#### `palladium.FindLib(libName)`
Finds the base address of a loaded native library.
*   **Returns**: `integer` (Base Address) or `0` if not found.

```lua
local base = palladium.FindLib("libil2cpp.so")
if base > 0 then
    palladium.Toast("Found Base: " .. string.format("%X", base))
end
```

---

### 2. 🔗 Pointer Chains
Chase multi-level pointers instantly without manual calculation.

#### `palladium.Pointer(base, offsets...)`
*   **Returns**: `integer` (Final Address)

```lua
-- Reads: [Base + 0x10] + 0x28 + 0x50
local ammoAddr = palladium.Pointer(base, 0x10, 0x28, 0x50)
```

---

### 3. 🔢 Memory Read/Write
Directly manipulate memory values.

| Command | Description | Example |
| :--- | :--- | :--- |
| **`ReadInt(addr)`** | Read 32-bit integer | `local hp = palladium.ReadInt(addr)` |
| **`WriteInt(addr, val)`** | Write 32-bit integer | `palladium.WriteInt(addr, 999)` |
| **`ReadFloat(addr)`** | Read 32-bit float | `local speed = palladium.ReadFloat(addr)` |
| **`WriteFloat(addr, val)`** | Write 32-bit float | `palladium.WriteFloat(addr, 5.0)` |
| **`ReadHex(addr, size)`** | Read raw bytes as hex string | `local hex = palladium.ReadHex(addr, 4)` |
| **`WriteHex(addr, hex)`** | Write raw hex string | `palladium.WriteHex(addr, "00 F0 20 E3")` |

---

### 4. 🪝 Native Hooking (Advanced)
Intercept game functions and modify CPU registers on the fly using **Dobby**.

#### `palladium.Hook(address, callback)`
*   **regs**: A table containing CPU registers (`x0`...`x28`, `sp`, `lr`).

```lua
local shootFunc = base + 0x123ABC

palladium.Hook(shootFunc, function(regs)
    -- regs.x1 usually holds ammo count
    regs.x1 = 999
    palladium.Toast("Unlimited Ammo Active! 🔫")
end)
```

---

### 5. ⚡ Utilities

*   **`palladium.Toast(msg)`**: Show a popup message.
*   **`palladium.Sleep(ms)`**: Pause script execution (essential for loops).

---

## 💡 Example Script: God Mode

```lua
palladium.Toast("Activating God Mode...")

local base = palladium.FindLib("libUE4.so")

if base > 0 then
    -- Calculate HP address
    local hpAddr = palladium.Pointer(base, 0x0250, 0x18, 0x300)
    
    -- Loop to keep HP full
    for i = 1, 100 do
        palladium.WriteInt(hpAddr, 1000)
        palladium.Sleep(1000) -- Wait 1 second
    end
else
    palladium.Toast("Game library not found!")
end
```

---
<div align="center">
  <p>Powered by <b>Palladium Engine</b></p>
</div>
