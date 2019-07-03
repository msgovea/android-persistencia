package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.BaseCallback;
import br.com.alura.estoque.retrofit.callback.BaseCallbackSemRetorno;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;

public class ProdutoRepository {

    private ProdutoDAO dao;
    private ProdutoService service;

    public ProdutoRepository(Context context) {
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        dao = db.getProdutoDAO();

        service = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosCallBack<List<Produto>> callback) {

        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    callback.quandoSucesso(resultado);
                    buscaProdutosAPI(callback);
                }).execute();
    }

    private void buscaProdutosAPI(DadosCarregadosCallBack<List<Produto>> callback) {
        Call<List<Produto>> call = service.buscaTodos();

        //evita asynctask com implementacao propria enqueue -- chamada service
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtosAPI) {
                new BaseAsyncTask<>(() -> {
                    dao.salva(produtosAPI);
                    return dao.buscaTodos();
                }, callback::quandoSucesso)
                        .execute();
            }

            @Override
            public void quandoFalha(String falha) {
                callback.quandoFalha(falha);
            }
        }));

    }

    public void salva(Produto produto, DadosCarregadosCallBack<Produto> callback) {

        Call<Produto> salva = service.salva(produto);
        salva.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoSalvo) {
                salvaProdutoLocal(produtoSalvo, callback);
            }

            @Override
            public void quandoFalha(String falha) {
                callback.quandoFalha(falha);
            }
        }));

    }

    private void salvaProdutoLocal(Produto produtoSalvo, DadosCarregadosCallBack<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.salva(produtoSalvo);
            return produtoSalvo;
        }, callback::quandoSucesso)
                .execute();
    }

    public void edita(Produto produto, DadosCarregadosCallBack<Produto> callback) {

        Call<Produto> call = service.edita(produto.getId(), produto);
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoAlterado) {
                editaLocal(produtoAlterado, callback);
            }

            @Override
            public void quandoFalha(String falha) {
                callback.quandoFalha(falha);
            }
        }));
    }

    private void editaLocal(Produto produto, DadosCarregadosCallBack<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::quandoSucesso)
                .execute();
    }

    public void removeProduto(Produto produto, DadosCarregadosCallBack callback) {
        Call<Void> call = service.remove(produto.getId());

        call.enqueue(new BaseCallbackSemRetorno(new BaseCallback.RespostaCallback<Void>() {
            @Override
            public void quandoSucesso(Void resposta) {
                removeInterno(produto, callback);
            }

            @Override
            public void quandoFalha(String falha) {
                callback.quandoFalha(falha);
            }
        }));
    }

    private void removeInterno(Produto produto, DadosCarregadosCallBack callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produto);
            return null;
        }, callback::quandoSucesso)
                .execute();
    }

    public interface DadosCarregadosCallBack<T> {
        void quandoSucesso(T resultado);

        void quandoFalha(String texto);
    }
}
