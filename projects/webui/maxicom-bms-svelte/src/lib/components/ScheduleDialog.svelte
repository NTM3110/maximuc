<script lang="ts">
  import { createEventDispatcher } from "svelte";
  import { createSchedule } from "$lib/services/schedule";

  export let stringId: string;
  export let open = false;
  interface $$Events {
    created: void;
  }

  const dispatch = createEventDispatcher<$$Events>();

  let startTime = "";
  let ratedCurrent: number | null = null;
  let isSubmitting = false;
  let error: string | null = null;

  function close() {
    // dispatch("close");
    open = false;
  }

  function reset() {
    startTime = "";
    ratedCurrent = "";
    error = null;
    isSubmitting = false;
  }

  async function submit() {
    error = null;

    if (!startTime) {
      error = "Start time is required";
      return;
    }

    const startDate = new Date(startTime);
    if (Number.isNaN(startDate.getTime())) {
      error = "Invalid start time";
      return;
    }

    const current = ratedCurrent ?? undefined;

    if (current !== undefined && !Number.isFinite(current)) {
      error = "Rated current must be a number";
      return;
    }

    try {
      isSubmitting = true;
      await createSchedule(stringId, startDate, current);
      dispatch("created");
      reset();
      close();
    } catch (e) {
      error = "Failed to create schedule";
    } finally {
      isSubmitting = false;
    }
  }
</script>

{#if open}
  <div class="modal-backdrop">
    <div class="schedule-modal">
      <div class="modal-header">
        <h5>Schedule Discharge</h5>
        <button class="close-btn" onclick={() => close()}>×</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label>Start Time</label>
          <input
            type="datetime-local"
            bind:value={startTime}
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label>Rated Current (A)</label>
          <input
            type="number"
            step="0.1"
            bind:value={ratedCurrent}
            class="form-control"
            placeholder="Optional"
          />
        </div>

        {#if error}
          <div class="error">{error}</div>
        {/if}
      </div>

      <div class="modal-footer">
        <button
          class="btn btn-secondary"
          onclick={() => close()}
          disabled={isSubmitting}
        >
          Cancel
        </button>
        <button
          class="btn btn-primary"
          onclick={() => submit()}
          disabled={isSubmitting}
        >
          {isSubmitting ? "Creating..." : "Create"}
        </button>
      </div>
    </div>
  </div>
{/if}

<style>
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1050;
  }

  .modal {
    background: #fff;
    border-radius: 6px;
    width: 420px;
    max-width: calc(100% - 2rem);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  }
  .schedule-modal {
    background: #fff;
    border-radius: 6px;
    width: 420px;
    max-width: calc(100% - 2rem);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    position: relative;
    z-index: 1060;
    }


  .modal-header,
  .modal-footer {
    padding: 1rem;
    border-bottom: 1px solid #dee2e6;
  }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .modal-body {
    padding: 1rem;
  }

  .modal-footer {
    border-top: 1px solid #dee2e6;
    border-bottom: none;
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
  }

  .form-group {
    margin-bottom: 1rem;
  }

  .form-control {
    width: 100%;
    padding: 0.375rem 0.5rem;
    font-size: 0.875rem;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 1.25rem;
    cursor: pointer;
  }

  .error {
    color: #dc3545;
    font-size: 0.875rem;
    margin-top: 0.5rem;
  }
</style>
