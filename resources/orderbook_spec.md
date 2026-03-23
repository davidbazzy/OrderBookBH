# Order Book — Behavioural Specification

## 1. Overview

The order book maintains two sorted queues of live orders — **buys** (bids) and **sells** (asks). When a new order arrives it first attempts to match against the opposite side. Any unmatched residual is inserted into the appropriate queue. After every accepted order the current book state is printed.

Please implement in Java or Rust. Use sample orders file as input to test.

---

## 2. Input Format

Each order is a comma-separated line:

```
Side,Id,Price,Quantity[,PeakSize]
```

| Field      | Type    | Description |
|------------|---------|-------------|
| `Side`     | char    | `B` = buy, `S` = sell |
| `Id`       | integer | Unique order identifier |
| `Price`    | integer | Limit price |
| `Quantity` | integer | Total order quantity |
| `PeakSize` | integer | **Optional.** Presence makes this an iceberg order (see §5) |

Lines that cannot be parsed are silently ignored.

---

## 3. Order Lifecycle

```
processOrder(line)
  │
  ├─ parse → null?  → discard silently
  │
  ├─ id already seen? → warn to stderr, discard (no display)
  │
  ├─ attempt matching (trade)
  │     └─ print trade confirmations (if any)
  │
  ├─ if order still has remaining quantity → insert into book
  │
  └─ display current book snapshot
```

---

## 4. Book Ordering (Price-Time Priority)

Each side is kept in a single sorted list:

- **Buy side** — highest price first. Orders at the same price appear in arrival order (FIFO).
- **Sell side** — lowest price first. Orders at the same price appear in arrival order (FIFO).

Insertion walks the list and places the new order immediately before the first existing order whose price is worse, preserving time order within a price level.

---

## 5. Order Types

### 5.1 Regular Order

Exposes its full remaining quantity at all times. Removed from the book when quantity reaches zero.

### 5.2 Iceberg Order

An order with a `PeakSize` field. Only the **peak** is visible in the book; the rest is hidden.

| Situation | Behaviour |
|-----------|-----------|
| Resting in book | Displays `min(peakSize, remaining)` as its visible quantity |
| Peak fully consumed in a matching round | Peak refills to `min(peakSize, newRemaining)` immediately; order stays live |
| Peak partially consumed | Visible quantity shrinks by the amount traded; no refill until the peak hits zero |
| Order acting as aggressor (incoming) | Trades against its **full** remaining quantity, not just the peak |
| Total quantity reaches zero | Order is removed from the book |

**Intra-round rotation.** When an iceberg's peak is exhausted during a single matching round, it is moved to the **back** of the execution queue for that price level. Other resting orders at the same price get to trade before the refilled peak is consumed again. This means a single incoming order can generate multiple fills against the same iceberg, interleaved with fills against other resting orders.

---

## 6. Matching Rules

### 6.1 Price Crossing

A new buy order matches a resting sell if the **sell price ≤ buy price**.
A new sell order matches a resting buy if the **buy price ≥ sell price**.


### 6.2 Matching Algorithm

Emit one trade confirmation per counterparty


`availableQty` for an iceberg acting as the passive counterparty is its current **visible peak**. When acting as the aggressor it is the full remaining quantity.

### 6.3 Duplicate Order IDs

Duplicated order id rejected.

---

## 7. Trade Confirmation Output

Printed to **stdout** after matching completes, one line per unique counterparty:

```
buyId,sellId,price,quantity
```

- `price` is always the **incoming (aggressive) order's** limit price.
- `quantity` is the **aggregate** fill against that counterparty across all intra-round slices (e.g. if an iceberg matched a counterparty twice in one round the two fills are summed into a single line).

---

## 8. Book Display

Printed to **stdout** after every accepted order (never for duplicates or unparseable lines).

```
+-----------------------------------------------------------------+
| BUY                            | SELL                           |
| Id       | Volume      | Price | Price | Volume      | Id       |
+----------+-------------+-------+-------+-------------+----------+
|    <id>  |   <volume>  | <px>  | <px>  |   <volume>  |  <id>   |
...
+-----------------------------------------------------------------+
```

- Rows are paired by depth level: row 0 = best bid vs best ask, row 1 = next level, etc.
- If one side has more levels than the other, the shorter side's extra rows are blank.
- Volume is formatted with thousands separators (e.g. `50,000`).

---

## 9. Worked Example

Input (`orders` file)
