PGDMP  6                    }            postgres    17.2    17.2     �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false            �           1262    5    postgres    DATABASE     |   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';
    DROP DATABASE postgres;
                     postgres    false            �           0    0    DATABASE postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                        postgres    false    4851            �            1259    16497    bank_account    TABLE     s   CREATE TABLE public.bank_account (
    user_id bigint NOT NULL,
    balance double precision DEFAULT 0 NOT NULL
);
     DROP TABLE public.bank_account;
       public         heap r       postgres    false            �            1259    16496    bank_account_user_id_seq    SEQUENCE     �   CREATE SEQUENCE public.bank_account_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 /   DROP SEQUENCE public.bank_account_user_id_seq;
       public               postgres    false    218            �           0    0    bank_account_user_id_seq    SEQUENCE OWNED BY     U   ALTER SEQUENCE public.bank_account_user_id_seq OWNED BY public.bank_account.user_id;
          public               postgres    false    217            W           2604    16504    bank_account user_id    DEFAULT     |   ALTER TABLE ONLY public.bank_account ALTER COLUMN user_id SET DEFAULT nextval('public.bank_account_user_id_seq'::regclass);
 C   ALTER TABLE public.bank_account ALTER COLUMN user_id DROP DEFAULT;
       public               postgres    false    217    218    218            �          0    16497    bank_account 
   TABLE DATA           8   COPY public.bank_account (user_id, balance) FROM stdin;
    public               postgres    false    218          �           0    0    bank_account_user_id_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.bank_account_user_id_seq', 3, true);
          public               postgres    false    217            Z           2606    16506    bank_account bank_account_pkey 
   CONSTRAINT     a   ALTER TABLE ONLY public.bank_account
    ADD CONSTRAINT bank_account_pkey PRIMARY KEY (user_id);
 H   ALTER TABLE ONLY public.bank_account DROP CONSTRAINT bank_account_pkey;
       public                 postgres    false    218            �      x�3�450�2�4�2�4445������ #�     