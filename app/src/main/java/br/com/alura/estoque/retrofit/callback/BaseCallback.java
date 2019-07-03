package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;


@EverythingIsNonNull
public class BaseCallback<T> implements Callback<T> {

    final RespostaCallback<T> callback;

    public BaseCallback(RespostaCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            T resultado = response.body();
            if (resultado != null) {
                callback.quandoSucesso(resultado);
            } else {
                callback.quandoFalha("Falha no retorno");
            }
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        callback.quandoFalha("Deu ruim! " + t.getMessage());
    }

    public interface RespostaCallback <T> {
        void quandoSucesso(T resposta);
        void quandoFalha(String falha);
    }
}
