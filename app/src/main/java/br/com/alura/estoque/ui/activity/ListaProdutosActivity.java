package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProdutoRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProdutoRepository produtoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        produtoRepository = new ProdutoRepository(this);

        produtoRepository.buscaProdutos(new ProdutoRepository.DadosCarregadosCallBack<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtos) {
                adapter.atualiza(produtos);
            }

            @Override
            public void quandoFalha(String texto) {
                exibeErro(texto);
            }
        });
    }



    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener((posicao, produtoSelecionado) -> {
            produtoRepository.removeProduto(produtoSelecionado, new ProdutoRepository.DadosCarregadosCallBack() {
                @Override
                public void quandoSucesso(Object resultado) {
                    adapter.remove(posicao);
                }

                @Override
                public void quandoFalha(String texto) {
                    exibeErro("Falha ao remover, sorry!");
                }
            });
        });
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, produto ->
            produtoRepository.salva(produto, new ProdutoRepository.DadosCarregadosCallBack<Produto>() {
                @Override
                public void quandoSucesso(Produto resultado) {
                    adapter.adiciona(resultado);
                }

                @Override
                public void quandoFalha(String texto) {
                    exibeErro(texto);
                }
            })
        ).mostra();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoCriado -> produtoRepository.edita(produtoCriado, new ProdutoRepository.DadosCarregadosCallBack<Produto>() {
                    @Override
                    public void quandoSucesso(Produto produtoAlterado) {
                        adapter.edita(posicao, produtoAlterado);
                    }

                    @Override
                    public void quandoFalha(String texto) {
                        exibeErro(texto);
                    }
                }))
                .mostra();
    }

    private void exibeErro(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

}
