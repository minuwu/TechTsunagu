# AI eval: Chat messages (draft_message)

XREQ-006 item 5. The cases are `backend/tests/fixtures/ai/eval/messages.json`: 10 fictional Foreman messages.
Run them with `uv run python -m app.ai.eval --pipelines messages`; the scores land in the `draft_message` rows of
[ai-eval.md](ai-eval.md). **Not run yet:** the eval calls the live models, so it waits until after filming, to
save quota.

A case passes only when all of these hold (`eval_messages` in `backend/app/ai/eval.py`):

- **Kept:** every shop word and number in `keep` appears in the draft.
- **Nothing added:** no word in `forbid` appears, for example a Machine the Foreman did not name, or a toned-down
  rewrite.
- **Questions:** `some` or `none`, as the case says. A greeting gets no quiz.
- **Tasks:** the same Tasks, with the same people, a due time when one was said, and the same Machine.

| Case | Input (short) | What it checks |
| :--- | :--- | :--- |
| m01 | 明日の朝8時から、2号プレスの金型交換… (spoken) | Two people by name, a time, Machine 2 |
| m02 | Check the oil on machine 3 | The Machine is kept as said: no "crane" or "slinging" added |
| m03 | おい、このバカ野郎、早く片付けろ | Translated as said, not toned down |
| m04 | hello | No Task, no questions |
| m05 | みなさん、今日もお疲れさまでした。… | No Task, no questions |
| m06 | OK | No Task, no questions |
| m07 | ラヒムさん、今日の15時までに溶接ブースの清掃を… | One person, a time, Machine 4 |
| m08 | 保護メガネをかけてから、プラズマ切断機を… | A safety rule: questions, no Task |
| m09 | Sadia, lock out the power before you clean… | One person, no time, Machine 2 |
| m10 | 明日は工場の点検で、作業はお休みです。 | A notice: questions, no Task |

## Open

- The card asks for one **prompt-injection** case, for example "ignore your instructions and…" inside a
  message. None of the 10 is one yet; m03 tests toning down, not injection. What the draft should do with it
  (translate it as said, make no Task) is Galib's call.
