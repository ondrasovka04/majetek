import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import ConfirmDialog from "@/components/card/ConfirmDialog";

describe("ConfirmDialog", () => {
  it("renders title and description when open", () => {
    render(<ConfirmDialog open={true} description="Opravdu smazat?" onCancel={vi.fn()} onConfirm={vi.fn()} />);

    screen.getByText("Potvrzení akce");
    screen.getByText("Opravdu smazat?");
  });

  it("renders custom title", () => {
    render(
      <ConfirmDialog open={true} title="Vlastní titul" description="Text" onCancel={vi.fn()} onConfirm={vi.fn()} />,
    );

    screen.getByText("Vlastní titul");
  });

  it("does not render content when closed", () => {
    render(<ConfirmDialog open={false} description="Skrytý text" onCancel={vi.fn()} onConfirm={vi.fn()} />);

    expect(screen.queryByText("Skrytý text")).toBeNull();
  });

  it("calls onConfirm when confirm button clicked", () => {
    const onConfirm = vi.fn();
    render(<ConfirmDialog open={true} description="Potvrdit?" onCancel={vi.fn()} onConfirm={onConfirm} />);

    fireEvent.click(screen.getByText("Potvrdit"));

    expect(onConfirm).toHaveBeenCalledOnce();
  });

  it("calls onCancel when cancel button clicked", () => {
    const onCancel = vi.fn();
    render(<ConfirmDialog open={true} description="Zrušit?" onCancel={onCancel} onConfirm={vi.fn()} />);

    fireEvent.click(screen.getByText("Zrušit"));

    expect(onCancel).toHaveBeenCalledOnce();
  });
});
