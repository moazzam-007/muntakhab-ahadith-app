package com.moazzam.muntakhabahadith.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.moazzam.muntakhabahadith.R;
import com.moazzam.muntakhabahadith.data.db.ImportedPdf;

/**
 * RecyclerView adapter for the imported PDF library list.
 * Uses {@link ListAdapter} for efficient diffing.
 */
public class ImportedPdfAdapter
        extends ListAdapter<ImportedPdf, ImportedPdfAdapter.ViewHolder> {

    public interface OnClickListener  { void onClick(ImportedPdf pdf); }
    public interface OnDeleteListener { void onDelete(ImportedPdf pdf); }

    private final OnClickListener  clickListener;
    private final OnDeleteListener deleteListener;

    public ImportedPdfAdapter(OnClickListener clickListener, OnDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener  = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_imported_pdf, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImportedPdf pdf = getItem(position);

        holder.tvName.setText(pdf.displayName);

        int percent = Math.round(pdf.progress * 100f);
        holder.tvProgress.setText(percent + "%");
        holder.progressBar.setProgress(percent);

        if (pdf.lastPage > 0) {
            holder.tvLastPage.setVisibility(View.VISIBLE);
            holder.tvLastPage.setText(
                holder.itemView.getContext().getString(R.string.last_seen_page, pdf.lastPage + 1));
        } else {
            holder.tvLastPage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onClick(pdf));

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(pdf));
        holder.btnDelete.setContentDescription(
            holder.itemView.getContext().getString(R.string.cd_delete_pdf, pdf.displayName));
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvName;
        TextView    tvProgress;
        TextView    tvLastPage;
        ProgressBar progressBar;
        Button      btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tv_pdf_name);
            tvProgress  = itemView.findViewById(R.id.tv_pdf_progress);
            tvLastPage  = itemView.findViewById(R.id.tv_last_page);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btnDelete   = itemView.findViewById(R.id.btn_delete);
        }
    }

    // ─── DiffUtil ─────────────────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<ImportedPdf> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ImportedPdf>() {
            @Override
            public boolean areItemsTheSame(@NonNull ImportedPdf a, @NonNull ImportedPdf b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ImportedPdf a, @NonNull ImportedPdf b) {
                return a.lastPage == b.lastPage
                    && Float.compare(a.progress, b.progress) == 0
                    && a.displayName.equals(b.displayName);
            }
        };
}
