# Anyone can open a shop and becomes its first Foreman

Decided 2026-11-24 by Minhaz. `POST /api/auth/register {shopName, displayName, pin}` is public and rate-limited (3 a minute per IP). It creates a Shop with a new 8-character Shop Code (letters and digits without 0, O, 1 and I, so it reads off a sticker), its first Foreman with the 6-digit PIN they chose (no forced change, since nobody else knows it), one group Thread 「全員」 and a copy of the standard Glossary from `seed_data/glossary.json`, then answers like a login plus `shopCode`. A new shop joins the first district until there is a district picker, and its Worker language is English (ADR 0016). Workers do not sign themselves up: their accounts still come from a Foreman (ADR 0010), so the Worker in a Check or Sign-off is still one a Foreman vouched for; every new account joins the shop's group Threads. We rejected Worker self-join by Shop Code because anyone holding the code could then create a Worker and forge Sign-offs, the record ADR 0010 protects, and we rejected cloning the demo shop because a real shop would start with someone else's Machines and messages.

## Amendment 2026-11-24: getting back into a shop

A forgotten random code locked owners out. Three ways back, all live:

- The app remembers the last shop code and role on the phone and fills the sign-in form with them.
- At registration the owner may choose the code (6 to 12 letters or digits, e.g. TECHTSUNAGU); a taken code is 409 `SHOP_CODE_TAKEN`. Left empty, the code stays random (8 characters). Shop codes are now 6 to 12 characters; district codes stay 8.
- `POST /api/auth/find` signs in with shop name + own name + PIN (both names ignore case). It answers only with the right PIN, uses the login rate limit and PIN lockout, so a shop name never reveals its code or roster.

## Amendment 2026-11-24: a daily limit per IP

Each new shop's Foreman gets a fresh AI allowance (100 requests a day), so 3 registrations a minute per IP let one person spend many days' quota. Decided by Anim with Asif's proposal: at most **10 new shops a day per IP** (`REGISTERS_PER_IP_PER_DAY`, `services/auth.py`), next to the 3 a minute; over it, `429 RATE_LIMITED` with `Retry-After`. No new table (the existing attempt counter, key `register-day:<ip>`); people in existing shops notice nothing. Limit: everyone behind one venue or mobile network shares an IP, so raise the number if judges register their own shops on site.
